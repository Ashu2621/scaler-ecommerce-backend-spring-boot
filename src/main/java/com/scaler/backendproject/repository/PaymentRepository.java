package com.scaler.backendproject.repository;

import com.scaler.backendproject.models.Payment;
import com.scaler.backendproject.models.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    @EntityGraph(attributePaths = "order")
    Optional<Payment> findByOrderId(String orderId);

    @EntityGraph(attributePaths = "order")
    Optional<Payment> findByProviderAndProviderReference(String provider, String providerReference);

    @EntityGraph(attributePaths = "order")
    List<Payment> findTop100ByStatusInAndUpdatedAtBefore(
            Collection<PaymentStatus> statuses, Instant updatedBefore);
}
