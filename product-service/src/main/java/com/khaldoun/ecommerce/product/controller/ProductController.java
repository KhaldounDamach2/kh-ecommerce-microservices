package com.khaldoun.ecommerce.product.controller;

import com.khaldoun.ecommerce.product.dto.AdjustStockRequest;
import com.khaldoun.ecommerce.product.dto.CreateProductRequest;
import com.khaldoun.ecommerce.product.dto.ProductResponse;
import com.khaldoun.ecommerce.product.dto.ProductSummaryResponse;
import com.khaldoun.ecommerce.product.dto.UpdateProductRequest;
import com.khaldoun.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request, userId));
    }

    @GetMapping
    public Page<ProductSummaryResponse> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return productService.listProducts(buildPageable(page, size, sort));
    }

    @GetMapping("/search")
    public Page<ProductSummaryResponse> searchProducts(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return productService.searchByName(query, buildPageable(page, size, sort));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('SELLER')")
    public Page<ProductSummaryResponse> getMyProducts(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return productService.getMyProducts(userId, buildPageable(page, size, sort));
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        return productService.updateProduct(id, request, userId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole) {
        productService.deleteProduct(id, userId);
        return ResponseEntity.noContent().build();
    }

    // Internal endpoint — no role check. Gateway/network isolation protects
    // external access. Called by order-service to adjust stock.
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Void> adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody AdjustStockRequest request) {
        productService.adjustStock(id, request.quantity(), request.operation());
        return ResponseEntity.noContent().build();
    }

    private Pageable buildPageable(int page, int size, String sort) {
        String[] sortParts = sort.split(",", 2);
        String field = sortParts[0].trim();
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1].trim())
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, field));
    }
}
