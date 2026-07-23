package com.scaler.backendproject.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 64) String sku,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 10_000) String description,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter uppercase ISO currency")
        String currency,
        @Min(0) int stockQuantity,
        @Size(max = 2048) String imageUrl,
        @NotBlank String categoryId
) {
}
