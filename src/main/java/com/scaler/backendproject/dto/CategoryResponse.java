package com.scaler.backendproject.dto;

public record CategoryResponse(
        String id,
        String name,
        String slug
) implements java.io.Serializable {
}
