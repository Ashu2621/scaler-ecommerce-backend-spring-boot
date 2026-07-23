package com.scaler.backendproject.repository;

import com.scaler.backendproject.models.OutboxEvent;
import com.scaler.backendproject.models.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event from OutboxEvent event
            where event.status = :status
              and event.nextAttemptAt <= :now
            order by event.createdAt
            """)
    List<OutboxEvent> findDispatchable(@Param("status") OutboxStatus status,
                                       @Param("now") Instant now,
                                       Pageable pageable);
}
