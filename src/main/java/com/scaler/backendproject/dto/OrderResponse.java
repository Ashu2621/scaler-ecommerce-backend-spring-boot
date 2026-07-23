package com.scaler.backendproject.dto;

import com.scaler.backendproject.models.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String id,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        List<Item> items,
        Instant createdAt,
        Instant updatedAt
) {
    public record Item(
            String productId,
            String sku,
            String title,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
    }
}
