package com.khaldoun.ecommerce.order.client.dto;

public record AdjustStockRequest(Integer quantity, String operation) {
}