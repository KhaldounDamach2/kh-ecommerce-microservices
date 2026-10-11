package com.khaldoun.ecommerce.order.event;

import java.time.Instant;
import java.util.UUID;

public record StockResultEvent(
        UUID eventId,
        Long orderId,
        boolean reserved,
        String reason,
        Instant occurredAt) {
}