package com.scaler.backendproject.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("app.security.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        Duration ttl
) {
}
