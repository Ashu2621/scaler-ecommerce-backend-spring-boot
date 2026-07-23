package com.scaler.backendproject.repository;

import com.scaler.backendproject.models.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, String> {
    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findByIdempotencyKeyAndCustomerEmail(String idempotencyKey, String customerEmail);

    @EntityGraph(attributePaths = "items")
    @Query("select o from CustomerOrder o where o.id = :id and o.customerEmail = :email")
    Optional<CustomerOrder> findOwnedById(@Param("id") String id, @Param("email") String email);

    Page<CustomerOrder> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail, Pageable pageable);
}
