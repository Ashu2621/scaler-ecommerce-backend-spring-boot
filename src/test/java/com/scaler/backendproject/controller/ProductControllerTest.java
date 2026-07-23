package com.scaler.backendproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.backendproject.dto.CategoryResponse;
import com.scaler.backendproject.dto.ProductResponse;
import com.scaler.backendproject.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {
    private ProductService productService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productService))
                .setValidator(validator)
                .build();
    }

    @Test
    void returnsProductAsJson() throws Exception {
        when(productService.findById("p1")).thenReturn(new ProductResponse(
                "p1", "SKU-1", "Phone", "Description", new BigDecimal("99.99"),
                "USD", 5, null, true,
                new CategoryResponse("c1", "Electronics", "electronics"),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        ));

        mockMvc.perform(get("/api/v1/products/p1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.category.slug").value("electronics"));
    }
}
