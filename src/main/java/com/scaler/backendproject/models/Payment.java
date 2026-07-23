package com.scaler.backendproject.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_order", columnNames = "order_id"),
                @UniqueConstraint(name = "uk_payments_provider_reference",
                        columnNames = {"provider", "provider_reference"})
        })
public class Payment extends BaseModel {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_order"))
    private CustomerOrder order;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(nullable = false, length = 255)
    private String providerReference;

    @Column(length = 2048)
    private String checkoutUrl;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    private Instant lastReconciledAt;

    protected Payment() {
    }

    public Payment(CustomerOrder order,
                   String provider,
                   String providerReference,
                   String checkoutUrl) {
        this.order = order;
        this.provider = provider;
        this.providerReference = providerReference;
        this.checkoutUrl = checkoutUrl;
        this.amount = order.getTotalAmount();
        this.currency = order.getCurrency();
    }

    public void apply(PaymentStatus newStatus) {
        if (status == PaymentStatus.SUCCEEDED) {
            return;
        }
        status = newStatus;
        lastReconciledAt = Instant.now();
    }

    public CustomerOrder getOrder() {
        return order;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getLastReconciledAt() {
        return lastReconciledAt;
    }
}
