package com.khaldoun.ecommerce.order.client;

import com.khaldoun.ecommerce.order.client.dto.ProductClientResponse;
import com.khaldoun.ecommerce.order.client.dto.AdjustStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductClientResponse getProduct(@PathVariable("id") Long id);

    @PatchMapping("/products/{id}/stock")
    void adjustStock(
            @PathVariable("id") Long id,
            @RequestBody AdjustStockRequest request);
}
