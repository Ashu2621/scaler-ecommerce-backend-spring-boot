package com.scaler.backendproject.dto;

import java.time.Instant;
import java.util.Set;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        String userId,
        String email,
        Set<String> roles
) {
}
