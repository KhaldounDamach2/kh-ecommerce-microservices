package com.khaldoun.ecommerce.product.service;

import com.khaldoun.ecommerce.product.domain.Product;
import com.khaldoun.ecommerce.product.dto.CreateProductRequest;
import com.khaldoun.ecommerce.product.dto.ProductResponse;
import com.khaldoun.ecommerce.product.dto.UpdateProductRequest;
import com.khaldoun.ecommerce.product.exception.InsufficientStockException;
import com.khaldoun.ecommerce.product.exception.InvalidPriceException;
import com.khaldoun.ecommerce.product.exception.NotProductOwnerException;
import com.khaldoun.ecommerce.product.exception.ProductNotFoundException;
import com.khaldoun.ecommerce.product.mapper.ProductMapper;
import com.khaldoun.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    // ---------------------------------------------------------------------
    // createProduct
    // ---------------------------------------------------------------------

    @Test
    void createProduct_setsSellerIdAndActiveTrue_andSaves() {
        CreateProductRequest request = new CreateProductRequest(
                "Laptop", "Nice laptop", new BigDecimal("999.99"), 10, "Electronics", null);
        Long sellerId = 42L;

        Product entityToSave = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .category(request.category())
                .build();

        Product savedEntity = Product.builder()
                .id(1L)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .category(request.category())
                .sellerId(sellerId)
                .active(true)
                .build();

        ProductResponse expectedResponse = new ProductResponse(
                1L, "Laptop", "Nice laptop", new BigDecimal("999.99"),
                10, "Electronics", null, sellerId, true, null, null);

        when(productMapper.toEntity(request)).thenReturn(entityToSave);
        when(productRepository.save(any(Product.class))).thenReturn(savedEntity);
        when(productMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        ProductResponse result = productService.createProduct(request, sellerId);

        assertThat(result).isEqualTo(expectedResponse);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product captured = captor.getValue();
        assertThat(captured.getSellerId()).isEqualTo(sellerId);
        assertThat(captured.getActive()).isTrue();
    }

    @Test
    void createProduct_throwsInvalidPriceException_whenPriceIsNegative() {
        CreateProductRequest request = new CreateProductRequest(
                "Laptop", "desc", new BigDecimal("-1.00"), 10, "Electronics", null);

        assertThatThrownBy(() -> productService.createProduct(request, 1L))
                .isInstanceOf(InvalidPriceException.class)
                .hasMessageContaining("negative");

        verify(productRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // getProductById
    // ---------------------------------------------------------------------

    @Test
    void getProductById_throwsProductNotFoundException_whenMissing() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------------
    // updateProduct
    // ---------------------------------------------------------------------

    @Test
    void updateProduct_throwsNotProductOwnerException_whenSellerDoesNotOwnProduct() {
        Long productId = 5L;
        Long ownerId = 1L;
        Long attackerId = 2L;

        Product existing = Product.builder()
                .id(productId)
                .sellerId(ownerId)
                .active(true)
                .price(new BigDecimal("50.00"))
                .build();

        UpdateProductRequest request = new UpdateProductRequest(
                "New name", null, new BigDecimal("60.00"), null, null, null);

        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> productService.updateProduct(productId, request, attackerId))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining(String.valueOf(productId));

        verify(productRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // deleteProduct (soft delete)
    // ---------------------------------------------------------------------

    @Test
    void deleteProduct_setsActiveFalse_andSaves() {
        Long productId = 5L;
        Long sellerId = 1L;

        Product existing = Product.builder()
                .id(productId)
                .sellerId(sellerId)
                .active(true)
                .build();

        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        productService.deleteProduct(productId, sellerId);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getActive()).isFalse();
    }

    // ---------------------------------------------------------------------
    // adjustStock
    // ---------------------------------------------------------------------

    @Test
    void adjustStock_throwsInsufficientStockException_whenDecrementWouldGoNegative() {
        Long productId = 5L;
        Product existing = Product.builder()
                .id(productId)
                .name("Laptop")
                .stock(3)
                .active(true)
                .build();

        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> productService.adjustStock(productId, 10, "DECREMENT"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Laptop");

        verify(productRepository, never()).save(any());
    }
}