package com.scaler.backendproject.events;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderPaidEvent(
        String eventId,
        String orderId,
        String customerEmail,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {
}
