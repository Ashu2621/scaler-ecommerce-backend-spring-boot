package com.scaler.backendproject.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        String id,
        String sku,
        String title,
        String description,
        BigDecimal price,
        String currency,
        int stockQuantity,
        String imageUrl,
        boolean active,
        CategoryResponse category,
        Instant createdAt,
        Instant updatedAt
) implements java.io.Serializable {
}
