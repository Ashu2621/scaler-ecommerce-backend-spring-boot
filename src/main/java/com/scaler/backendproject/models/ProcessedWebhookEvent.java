package com.scaler.backendproject.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "processed_webhook_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_webhook_provider_event",
                columnNames = {"provider", "provider_event_id"}))
public class ProcessedWebhookEvent extends BaseModel {
    @Column(nullable = false, length = 30)
    private String provider;

    @Column(name = "provider_event_id", nullable = false, length = 255)
    private String providerEventId;

    protected ProcessedWebhookEvent() {
    }

    public ProcessedWebhookEvent(String provider, String providerEventId) {
        this.provider = provider;
        this.providerEventId = providerEventId;
    }
}
