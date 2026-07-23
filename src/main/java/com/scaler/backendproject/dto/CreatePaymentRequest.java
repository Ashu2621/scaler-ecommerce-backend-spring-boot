package com.scaler.backendproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePaymentRequest(
        @NotBlank @Size(max = 30) String provider
) {
}
