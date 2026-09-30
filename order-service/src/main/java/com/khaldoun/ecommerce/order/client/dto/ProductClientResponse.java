package com.khaldoun.ecommerce.order.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductClientResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String category,
        String imageUrl,
        Long sellerId,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
