package com.scaler.backendproject.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @Size(max = 200) String title,
        @Size(max = 10_000) String description,
        @DecimalMin("0.01") BigDecimal price,
        @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter uppercase ISO currency")
        String currency,
        @Min(0) Integer stockQuantity,
        @Size(max = 2048) String imageUrl,
        String categoryId,
        Boolean active
) {
}
