package com.khaldoun.ecommerce.product.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Long orderId,
        Long customerId,
        Instant occurredAt,
        List<OrderLine> items) {

    public record OrderLine(Long productId, Integer quantity) {
    }
}