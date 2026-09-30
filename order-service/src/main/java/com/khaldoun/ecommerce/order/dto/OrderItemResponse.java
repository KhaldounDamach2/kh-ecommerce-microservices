package com.khaldoun.ecommerce.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productImage,
        Long sellerId,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}
