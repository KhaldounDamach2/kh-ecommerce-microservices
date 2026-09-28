package com.khaldoun.ecommerce.product.service;

import com.khaldoun.ecommerce.product.domain.Product;
import com.khaldoun.ecommerce.product.dto.CreateProductRequest;
import com.khaldoun.ecommerce.product.dto.ProductResponse;
import com.khaldoun.ecommerce.product.dto.ProductSummaryResponse;
import com.khaldoun.ecommerce.product.dto.UpdateProductRequest;
import com.khaldoun.ecommerce.product.exception.InvalidPriceException;
import com.khaldoun.ecommerce.product.exception.NotProductOwnerException;
import com.khaldoun.ecommerce.product.exception.ProductNotFoundException;
import com.khaldoun.ecommerce.product.mapper.ProductMapper;
import com.khaldoun.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request, Long sellerId) {
        validatePrice(request.price());

        Product product = productMapper.toEntity(request);
        product.setSellerId(sellerId);
        product.setActive(true);

        Product savedProduct = productRepository.save(product);
        log.info("Created product with id {} for seller {}", savedProduct.getId(), sellerId);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> listProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(productMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> getMyProducts(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable)
                .map(productMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryResponse> searchByName(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return listProducts(pageable);
        }

        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(q.trim(), pageable)
                .map(productMapper::toSummaryResponse);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, UpdateProductRequest request, Long sellerId) {
        validatePrice(request.price());

        Product product = findActiveProduct(id);
        verifyOwnership(product, sellerId);
        productMapper.updateEntityFromRequest(request, product);

        Product savedProduct = productRepository.save(product);
        log.info("Updated product with id {} for seller {}", id, sellerId);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional
    public void deleteProduct(Long id, Long sellerId) {
        Product product = findActiveProduct(id);
        verifyOwnership(product, sellerId);
        product.setActive(false);
        productRepository.save(product);
        log.info("Deleted product with id {} for seller {}", id, sellerId);
    }

    @Transactional(readOnly = true)
    public long countMyProducts(Long sellerId) {
        return productRepository.countBySellerId(sellerId);
    }

    private Product findActiveProduct(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    private void verifyOwnership(Product product, Long sellerId) {
        if (!Objects.equals(product.getSellerId(), sellerId)) {
            throw new NotProductOwnerException("Seller does not own product with id: " + product.getId());
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceException("Product price cannot be negative");
        }
    }
}
