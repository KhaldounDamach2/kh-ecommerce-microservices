package com.khaldoun.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @Size(max = 200)
        String name,
        @Size(max = 5000)
        String description,
        @DecimalMin("0.00")
        BigDecimal price,
        @Min(0)
        Integer stock,
        @Size(max = 100)
        String category,
        @Size(max = 500)
        String imageUrl
) {
}
