package com.scaler.backendproject.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.backendproject.configs.MockPaymentProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MockPaymentGatewayTest {
    @Test
    void rejectsUnsignedWebhook() {
        MockPaymentGateway gateway = new MockPaymentGateway(
                new MockPaymentProperties("secret", "http://checkout"),
                new ObjectMapper()
        );

        assertThatThrownBy(() -> gateway.verifyAndParse(
                "{\"eventId\":\"evt-1\",\"paymentReference\":\"pay-1\",\"status\":\"SUCCEEDED\"}",
                "wrong"))
                .isInstanceOf(InvalidWebhookException.class)
                .hasMessageContaining("signature");
    }
}
