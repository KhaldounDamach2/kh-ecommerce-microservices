package com.khaldoun.ecommerce.product.dto;

import java.math.BigDecimal;

public record ProductSummaryResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stock,
        String category,
        String imageUrl,
        Long sellerId
) {
}
