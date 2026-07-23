package com.scaler.backendproject.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.backendproject.configs.MockPaymentProperties;
import com.scaler.backendproject.models.PaymentStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.payment.mock.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MockPaymentProperties.class)
public class MockPaymentGateway implements PaymentGateway {
    private final MockPaymentProperties properties;
    private final ObjectMapper objectMapper;

    public MockPaymentGateway(MockPaymentProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String provider() {
        return "mock";
    }

    @Override
    public CheckoutSession createCheckout(CheckoutCommand command) {
        String reference = "mock_" + UUID.randomUUID();
        return new CheckoutSession(reference, properties.checkoutBaseUrl() + "/" + reference);
    }

    @Override
    public WebhookEvent verifyAndParse(String payload, String signature) {
        if (signature == null || !MessageDigest.isEqual(
                hmac(payload).getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidWebhookException("INVALID_MOCK_WEBHOOK", "Mock webhook signature is invalid");
        }
        try {
            MockWebhook webhook = objectMapper.readValue(payload, MockWebhook.class);
            return new WebhookEvent(webhook.eventId(), webhook.paymentReference(), webhook.status());
        } catch (Exception exception) {
            throw new InvalidWebhookException("INVALID_MOCK_WEBHOOK", "Mock webhook payload is invalid", exception);
        }
    }

    @Override
    public PaymentStatus fetchStatus(String providerReference) {
        return PaymentStatus.PENDING;
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    properties.webhookSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot calculate mock webhook signature", exception);
        }
    }

    private record MockWebhook(
            String eventId,
            String paymentReference,
            PaymentStatus status
    ) {
    }
}
