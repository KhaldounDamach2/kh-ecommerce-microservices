package com.khaldoun.ecommerce.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.khaldoun.ecommerce.order.client.ProductClient;
import com.khaldoun.ecommerce.order.client.dto.AdjustStockRequest;
import com.khaldoun.ecommerce.order.client.dto.ProductClientResponse;
import com.khaldoun.ecommerce.order.domain.Order;
import com.khaldoun.ecommerce.order.domain.OrderItem;
import com.khaldoun.ecommerce.order.domain.OrderStatus;
import com.khaldoun.ecommerce.order.dto.CreateOrderRequest;
import com.khaldoun.ecommerce.order.dto.OrderItemRequest;
import com.khaldoun.ecommerce.order.dto.OrderResponse;
import com.khaldoun.ecommerce.order.dto.UpdateOrderStatusRequest;
import com.khaldoun.ecommerce.order.exception.InsufficientStockException;
import com.khaldoun.ecommerce.order.exception.InvalidOrderStatusException;
import com.khaldoun.ecommerce.order.exception.OrderNotFoundException;
import com.khaldoun.ecommerce.order.exception.ProductNotFoundException;
import com.khaldoun.ecommerce.order.mapper.OrderMapper;
import com.khaldoun.ecommerce.order.repository.OrderItemRepository;
import com.khaldoun.ecommerce.order.repository.OrderRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request, Long customerId) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            Long productId = itemRequest.productId();
            ProductClientResponse product;
            try {
                product = productClient.getProduct(productId);
            } catch (FeignException.NotFound exception) {
                throw new ProductNotFoundException("Product not found with id: " + productId);
            } catch (FeignException exception) {
                log.warn("Failed to fetch product {}", productId, exception);
                throw new RuntimeException("Failed to fetch product " + productId, exception);
            }

            if (Boolean.FALSE.equals(product.active())) {
                throw new ProductNotFoundException("Product not found with id: " + productId);
            }
            if (product.stock() < itemRequest.quantity()) {
                throw new InsufficientStockException("Not enough stock for product: " + product.name());
            }

            BigDecimal subtotal = product.price().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            OrderItem orderItem = OrderItem.builder()
                    .productId(productId)
                    .productName(product.name())
                    .productImage(product.imageUrl())
                    .sellerId(product.sellerId())
                    .unitPrice(product.price())
                    .quantity(itemRequest.quantity())
                    .subtotal(subtotal)
                    .build();
            items.add(orderItem);
            total = total.add(subtotal);
        }

        Order order = Order.builder()
                .customerId(customerId)
                .totalPrice(total)
                .status(OrderStatus.PENDING)
                .build();
        items.forEach(order::addItem);

        Order saved = orderRepository.save(order);
        // Stock decrement is best-effort. If it fails, we log and continue —
        // the order is already placed. A production system would use the SAGA
        // pattern with compensating transactions.
        for (OrderItem item : saved.getItems()) {
            try {
                productClient.adjustStock(
                        item.getProductId(),
                        new AdjustStockRequest(item.getQuantity(), "DECREMENT"));
            } catch (FeignException exception) {
                log.error("Failed to decrement stock for product {} (order {}): {}",
                        item.getProductId(), saved.getId(), exception.getMessage());
            }
        }
        log.info("Placed order {} for customer {}", saved.getId(), customerId);
        return orderMapper.toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getSellerOrders(Long sellerId, Pageable pageable) {
        return orderRepository.findOrdersBySellerId(sellerId, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, Long userId, String role) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if ("CUSTOMER".equals(role) && !order.getCustomerId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        if ("SELLER".equals(role)
                && order.getItems().stream().noneMatch(item -> item.getSellerId().equals(userId))) {
            throw new AccessDeniedException("Access denied");
        }
        if (!"CUSTOMER".equals(role) && !"SELLER".equals(role) && !"ADMIN".equals(role)) {
            throw new AccessDeniedException("Access denied");
        }

        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        boolean ownsItem = order.getItems().stream()
                .anyMatch(item -> item.getSellerId().equals(sellerId));
        if (!ownsItem) {
            throw new AccessDeniedException("Access denied");
        }

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.status().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidOrderStatusException("Invalid status: " + request.status());
        }

        OrderStatus currentStatus = order.getStatus();
        boolean allowedTransition = (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED)
                || (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.SHIPPED)
                || (currentStatus == OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED);
        if (!allowedTransition) {
            throw new InvalidOrderStatusException(
                    "Cannot transition from " + currentStatus + " to " + newStatus);
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Updated order {} status from {} to {} by seller {}",
                orderId, currentStatus, newStatus, sellerId);
        return orderMapper.toOrderResponse(saved);
    }
}
