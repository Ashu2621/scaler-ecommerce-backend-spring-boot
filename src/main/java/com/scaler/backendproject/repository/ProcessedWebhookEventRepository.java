package com.scaler.backendproject.repository;

import com.scaler.backendproject.models.ProcessedWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedWebhookEventRepository extends JpaRepository<ProcessedWebhookEvent, String> {
    boolean existsByProviderAndProviderEventId(String provider, String providerEventId);
}
