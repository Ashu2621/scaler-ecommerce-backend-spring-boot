package com.scaler.backendproject.payment;

import com.scaler.backendproject.models.PaymentStatus;

import java.math.BigDecimal;

public interface PaymentGateway {
    String provider();

    CheckoutSession createCheckout(CheckoutCommand command);

    WebhookEvent verifyAndParse(String payload, String signature);

    PaymentStatus fetchStatus(String providerReference);

    record CheckoutCommand(
            String orderId,
            BigDecimal amount,
            String currency,
            String description
    ) {
    }

    record CheckoutSession(
            String providerReference,
            String checkoutUrl
    ) {
    }

    record WebhookEvent(
            String providerEventId,
            String providerReference,
            PaymentStatus status
    ) {
    }
}
