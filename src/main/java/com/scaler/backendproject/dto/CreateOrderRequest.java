package com.scaler.backendproject.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<@Valid Item> items
) {
    public record Item(
            @NotBlank String productId,
            @Min(1) int quantity
    ) {
    }
}
