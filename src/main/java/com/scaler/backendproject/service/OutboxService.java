package com.scaler.backendproject.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.backendproject.events.OrderPaidEvent;
import com.scaler.backendproject.models.OutboxEvent;
import com.scaler.backendproject.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {
    public static final String ORDER_PAID = "ORDER_PAID";

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void enqueue(OrderPaidEvent event) {
        try {
            repository.save(new OutboxEvent(
                    event.orderId(),
                    ORDER_PAID,
                    objectMapper.writeValueAsString(event)
            ));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize order-paid event", exception);
        }
    }
}
