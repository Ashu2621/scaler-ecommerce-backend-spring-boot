package com.scaler.backendproject.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.payment.stripe")
public record StripeProperties(
        String apiKey,
        String webhookSecret,
        String successUrl,
        String cancelUrl
) {
}
