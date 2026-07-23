package com.scaler.backendproject.service;

import com.scaler.backendproject.dto.ProductRequest;
import com.scaler.backendproject.exceptions.ConflictException;
import com.scaler.backendproject.models.Category;
import com.scaler.backendproject.models.Product;
import com.scaler.backendproject.repository.CategoryRepository;
import com.scaler.backendproject.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        CatalogMapper mapper = new CatalogMapper();
        productService = new ProductService(productRepository, categoryRepository, mapper);
    }

    @Test
    void createsProductWithServerManagedCategory() {
        Category category = new Category("Electronics", "electronics");
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = productService.create(new ProductRequest(
                "phone-1",
                "Phone",
                "A reliable phone",
                new BigDecimal("499.00"),
                "USD",
                10,
                "https://example.com/phone.png",
                "category-1"
        ));

        assertThat(response.sku()).isEqualTo("PHONE-1");
        assertThat(response.price()).isEqualByComparingTo("499.00");
        assertThat(response.category().name()).isEqualTo("Electronics");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void rejectsDuplicateSku() {
        when(productRepository.existsBySkuIgnoreCase("PHONE-1")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(new ProductRequest(
                "PHONE-1", "Phone", "Description", BigDecimal.ONE,
                "USD", 1, null, "category-1")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }
}
