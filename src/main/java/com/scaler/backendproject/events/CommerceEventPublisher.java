package com.scaler.backendproject.events;

public interface CommerceEventPublisher {
    void publish(OrderPaidEvent event);
}
