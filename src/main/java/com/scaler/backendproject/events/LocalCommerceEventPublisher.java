package com.scaler.backendproject.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.provider", havingValue = "local", matchIfMissing = true)
public class LocalCommerceEventPublisher implements CommerceEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(LocalCommerceEventPublisher.class);

    @Override
    public void publish(OrderPaidEvent event) {
        log.info("Published local order-paid event orderId={} customer={}",
                event.orderId(), event.customerEmail());
    }
}
