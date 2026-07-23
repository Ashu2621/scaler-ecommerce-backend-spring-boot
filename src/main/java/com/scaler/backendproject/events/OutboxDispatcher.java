package com.scaler.backendproject.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.backendproject.models.OutboxEvent;
import com.scaler.backendproject.models.OutboxStatus;
import com.scaler.backendproject.repository.OutboxEventRepository;
import com.scaler.backendproject.service.NotificationService;
import com.scaler.backendproject.service.OutboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private final OutboxEventRepository repository;
    private final CommerceEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public OutboxDispatcher(OutboxEventRepository repository,
                            CommerceEventPublisher eventPublisher,
                            NotificationService notificationService,
                            ObjectMapper objectMapper) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.messaging.outbox-delay-ms:1000}")
    @Transactional
    public void dispatch() {
        List<OutboxEvent> events = repository.findDispatchable(
                OutboxStatus.PENDING, Instant.now(), PageRequest.of(0, 100));
        for (OutboxEvent outbox : events) {
            try {
                if (OutboxService.ORDER_PAID.equals(outbox.getEventType())) {
                    OrderPaidEvent event = objectMapper.readValue(
                            outbox.getPayload(), OrderPaidEvent.class);
                    eventPublisher.publish(event);
                    notificationService.sendPaymentReceipt(event);
                }
                outbox.published();
            } catch (Exception exception) {
                outbox.failed();
                log.warn("Outbox delivery failed eventId={} attempt={}",
                        outbox.getId(), outbox.getAttempts(), exception);
            }
            repository.save(outbox);
        }
    }
}
