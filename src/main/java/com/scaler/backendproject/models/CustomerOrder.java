package com.scaler.backendproject.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "customer_orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_orders_customer_idempotency",
                columnNames = {"customer_email", "idempotency_key"}))
public class CustomerOrder extends BaseModel {
    @Column(nullable = false, length = 255)
    private String customerEmail;

    @Column(nullable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt ASC")
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String customerEmail, String idempotencyKey, String currency) {
        this.customerEmail = customerEmail;
        this.idempotencyKey = idempotencyKey;
        this.currency = currency;
    }

    public void addItem(Product product, int quantity) {
        OrderItem item = new OrderItem(this, product, quantity);
        items.add(item);
        totalAmount = totalAmount.add(item.getLineTotal());
    }

    public void markPaid() {
        status = OrderStatus.PAID;
    }

    public void markPaymentFailed() {
        if (status != OrderStatus.PAID) {
            status = OrderStatus.PAYMENT_FAILED;
        }
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
