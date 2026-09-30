package com.khaldoun.ecommerce.order.client;

import com.khaldoun.ecommerce.order.client.dto.ProductClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductClientResponse getProduct(@PathVariable("id") Long id);
}
