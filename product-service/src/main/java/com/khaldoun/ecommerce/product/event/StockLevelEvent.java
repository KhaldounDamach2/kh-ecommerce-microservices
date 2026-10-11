package com.khaldoun.ecommerce.product.event;

import java.time.Instant;
import java.util.UUID;

// Sent on stock-low or stock-depleted; the topic tells which one.
public record StockLevelEvent(
        UUID eventId,
        Long productId,
        String productName,
        Long sellerId,
        int stock,
        Instant occurredAt) {
}
