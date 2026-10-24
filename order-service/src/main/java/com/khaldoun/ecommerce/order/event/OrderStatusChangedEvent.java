package com.khaldoun.ecommerce.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// A fact: "order X is now in status Y". The listeners decide whom to tell.
// sellerIds: a marketplace order can contain items of several sellers.
// reason: only set for CANCELLED, null otherwise.
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
