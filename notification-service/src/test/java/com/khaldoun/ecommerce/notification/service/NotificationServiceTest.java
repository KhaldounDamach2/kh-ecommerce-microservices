package com.khaldoun.ecommerce.notification.service;

import com.khaldoun.ecommerce.notification.exception.NotificationNotFoundException;
import com.khaldoun.ecommerce.notification.domain.Notification;
import com.khaldoun.ecommerce.notification.domain.NotificationType;
import com.khaldoun.ecommerce.notification.domain.ProcessedEvent;
import com.khaldoun.ecommerce.notification.domain.UserContact;
import com.khaldoun.ecommerce.notification.event.OrderStatusChangedEvent;
import com.khaldoun.ecommerce.notification.event.StockLevelEvent;
import com.khaldoun.ecommerce.notification.repository.NotificationRepository;
import com.khaldoun.ecommerce.notification.repository.ProcessedEventRepository;
import com.khaldoun.ecommerce.notification.repository.UserContactRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private UserContactRepository userContactRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationService notificationService;

    private final StockLevelEvent lowStock = new StockLevelEvent(
            UUID.randomUUID(), 8L, "Asus Laptop", 11L, 3, Instant.now());

    @Test
    void notifySellerAboutStock_savesNotificationForSeller() {
        notificationService.notifySellerAboutStock(lowStock, NotificationType.STOCK_LOW);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(11L);
        assertThat(saved.getType()).isEqualTo(NotificationType.STOCK_LOW);
        assertThat(saved.getMessage()).isEqualTo("Only 3 left of Asus Laptop.");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    void notifySellerAboutStock_skipsDuplicateEvent() {
        when(processedEventRepository.existsById(lowStock.eventId())).thenReturn(true);

        notificationService.notifySellerAboutStock(lowStock, NotificationType.STOCK_LOW);

        verify(notificationRepository, never()).save(any());
    }

    // Tests for markAsRead method
    @Test
    void markAsRead_setsReadTrue_forOwnNotification() {
        Notification mine = Notification.builder().id(1L).userId(11L).read(false).build();
        when(notificationRepository.findByIdAndUserId(1L, 11L)).thenReturn(Optional.of(mine));

        notificationService.markAsRead(1L, 11L);

        assertThat(mine.isRead()).isTrue();
    }

    // Tests for markAllAsRead method
    @Test
    void markAsRead_throwsNotFound_forSomeoneElsesNotification() {
        // Notification 1 belongs to seller 11; user 99 tries to touch it
        when(notificationRepository.findByIdAndUserId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 99L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    // ---- order status notifications ----

    // Order 44 of customer 3, with items of sellers 15 and 9.
    private OrderStatusChangedEvent orderEvent(String newStatus, String reason) {
        return new OrderStatusChangedEvent(UUID.randomUUID(), 44L, 3L, List.of(15L, 9L),
                newStatus, reason, new BigDecimal("225.00"), Instant.now());
    }

    @Test
    void notifyAboutOrderStatus_confirmed_tellsTheCustomer_inAppAndByEmail() {
        // given: we know the customer's address
        OrderStatusChangedEvent event = orderEvent("CONFIRMED", null);
        when(userContactRepository.findById(3L))
                .thenReturn(Optional.of(new UserContact(3L, "test@example.com", "CUSTOMER", Instant.now())));

        // when
        notificationService.notifyAboutOrderStatus(event);

        // then: one bell for the customer, pointing to the order ...
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(3L);
        assertThat(saved.getType()).isEqualTo(NotificationType.ORDER_CONFIRMED);
        assertThat(saved.getOrderId()).isEqualTo(44L);
        assertThat(saved.getProductId()).isNull();

        // ... one email, and the event is remembered
        verify(emailService).send("test@example.com", "Order #44 confirmed", saved.getMessage());
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void notifyAboutOrderStatus_delivered_tellsEachSeller() {
        // orderEvent("DELIVERED", null), no contacts known
        // then: notificationRepository.save called twice (times(2)), for
        // users 15 and 9, both ORDER_DELIVERED; nothing for the customer
        notificationService.notifyAboutOrderStatus(orderEvent("DELIVERED", null));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(Notification::getUserId)
                .containsExactly(15L, 9L);
        assertThat(captor.getAllValues())
                .allMatch(n -> n.getType() == NotificationType.ORDER_DELIVERED);
        verify(emailService, never()).send(any(), any(), any());

    }

    @Test
    void notifyAboutOrderStatus_cancelled_containsTheReason() {
        // orderEvent("CANCELLED", "Not enough stock")
        // then: the saved notification's message contains the reason
        notificationService.notifyAboutOrderStatus(orderEvent("CANCELLED", "Not enough stock"));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.ORDER_CANCELLED);
        assertThat(captor.getValue().getMessage()).contains("Not enough stock");

    }

    @Test
    void notifyAboutOrderStatus_skipsDuplicateEvent() {
        // processedEventRepository.existsById(...) returns true
        // then: no notification, no email
        OrderStatusChangedEvent event = orderEvent("SHIPPED", null);
        when(processedEventRepository.existsById(event.eventId())).thenReturn(true);

        notificationService.notifyAboutOrderStatus(event);

        verify(notificationRepository, never()).save(any());
        verifyNoInteractions(emailService);

    }

    @Test
    void notifyAboutOrderStatus_unknownStatus_notifiesNobody_butRemembersEvent() {
        // orderEvent("PENDING", null)
        // then: no notification, but the event is remembered as processed
        notificationService.notifyAboutOrderStatus(orderEvent("PENDING", null));

        verify(notificationRepository, never()).save(any());
        verify(processedEventRepository).save(any(ProcessedEvent.class));

    }
}
