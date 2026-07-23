package com.scaler.backendproject.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.payment.mock")
public record MockPaymentProperties(
        String webhookSecret,
        String checkoutBaseUrl
) {
}
