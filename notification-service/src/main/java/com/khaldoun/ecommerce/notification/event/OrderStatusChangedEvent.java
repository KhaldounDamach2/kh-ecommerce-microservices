package com.khaldoun.ecommerce.notification.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Sent by order-service on every status change: "order X is now Y".
// reason is only set for CANCELLED.
public record OrderStatusChangedEvent(
        UUID eventId,
        Long orderId,
        Long customerId,
        List<Long> sellerIds,
        String newStatus,
        String reason,
        BigDecimal totalPrice,
        Instant occurredAt) {
}
