package com.khaldoun.ecommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import com.khaldoun.ecommerce.order.client.ProductClient;
import com.khaldoun.ecommerce.order.domain.Order;
import com.khaldoun.ecommerce.order.domain.OrderItem;
import com.khaldoun.ecommerce.order.domain.OrderStatus;
import com.khaldoun.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.khaldoun.ecommerce.order.event.OrderStatusChangedEvent;
import com.khaldoun.ecommerce.order.exception.InvalidOrderStatusException;
import com.khaldoun.ecommerce.order.mapper.OrderMapper;
import com.khaldoun.ecommerce.order.repository.OrderItemRepository;
import com.khaldoun.ecommerce.order.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Long CUSTOMER = 3L;
    private static final Long OTHER_CUSTOMER = 17L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    // An order of CUSTOMER with one item per seller (seller 15 twice on purpose:
    // the event must still name it only once).
    private Order order(OrderStatus status) {
        Order order = Order.builder()
                .id(44L)
                .customerId(CUSTOMER)
                .totalPrice(new BigDecimal("225.00"))
                .status(status)
                .build();
        for (Long sellerId : new Long[] { 15L, 15L, 9L }) {
            order.addItem(OrderItem.builder()
                    .productId(12L)
                    .productName("sony TV")
                    .sellerId(sellerId)
                    .unitPrice(new BigDecimal("75.00"))
                    .quantity(1)
                    .subtotal(new BigDecimal("75.00"))
                    .build());
        }
        return order;
    }

    // The fake database returns this order and saves whatever it gets.
    private void givenStored(Order order) {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
    }

    private void givenSaveReturnsTheOrder() {
        when(orderRepository.save(any(Order.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void confirmReceipt_deliversShippedOrder_andAnnouncesIt() {
        // given
        Order order = order(OrderStatus.SHIPPED);
        givenStored(order);
        givenSaveReturnsTheOrder();

        // when
        orderService.confirmReceipt(44L, CUSTOMER);

        // then: the order is DELIVERED ...
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);

        // ... and exactly this fact was handed to Spring for Kafka
        ArgumentCaptor<OrderStatusChangedEvent> captor = ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        OrderStatusChangedEvent event = captor.getValue();
        assertThat(event.orderId()).isEqualTo(44L);
        assertThat(event.customerId()).isEqualTo(CUSTOMER);
        assertThat(event.newStatus()).isEqualTo("DELIVERED");
        assertThat(event.sellerIds()).containsExactly(15L, 9L);
        assertThat(event.reason()).isNull();
        assertThat(event.eventId()).isNotNull();
    }

    @Test
    void confirmReceipt_rejectsSomeoneElsesOrder() {
        // a SHIPPED order, but OTHER_CUSTOMER calls confirmReceipt
        // then: AccessDeniedException, still SHIPPED, nothing saved, no event
        Order order = order(OrderStatus.SHIPPED);
        givenStored(order);

        assertThatThrownBy(() -> orderService.confirmReceipt(44L, OTHER_CUSTOMER))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);

    }

    @Test
    void confirmReceipt_rejectsOrderThatIsNotShipped() {
        // a CONFIRMED order, the right customer
        // then: InvalidOrderStatusException, nothing saved, no event
        Order order = order(OrderStatus.CONFIRMED);
        givenStored(order);

        assertThatThrownBy(() -> orderService.confirmReceipt(44L, CUSTOMER))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("CONFIRMED");

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);

    }

    @Test
    void updateStatus_sellerCanNoLongerDeliver() {
        // DELIVERED belongs to the customer since N3
        Order order = order(OrderStatus.SHIPPED);
        givenStored(order);

        assertThatThrownBy(() -> orderService.updateStatus(44L, new UpdateOrderStatusRequest("DELIVERED"), 15L))
                .isInstanceOf(InvalidOrderStatusException.class);

        verifyNoInteractions(eventPublisher);

    }

    @Test
    void cancelIfRejected_announcesCancellationWithReason() {
        // a PENDING order, cancelIfRejected(44L, "Not enough stock")
        // then: CANCELLED, one event with newStatus "CANCELLED" and the reason
        Order order = order(OrderStatus.PENDING);
        givenStored(order);

        orderService.cancelIfRejected(44L, "Not enough stock");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        ArgumentCaptor<OrderStatusChangedEvent> captor = ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().newStatus()).isEqualTo("CANCELLED");
        assertThat(captor.getValue().reason()).isEqualTo("Not enough stock");

    }

    @Test
    void cancelIfRejected_redeliveredMessageIsNotAnnouncedTwice() {
        // an order that is already CANCELLED, cancelIfRejected again
        // then: no save, no event
        Order order = order(OrderStatus.CANCELLED);
        givenStored(order);

        orderService.cancelIfRejected(44L, "Not enough stock");

        verify(orderRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);

    }
}
