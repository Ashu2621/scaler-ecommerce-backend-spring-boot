package com.scaler.backendproject.models;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "idx_products_title", columnList = "title"),
                @Index(name = "idx_products_category", columnList = "category_id")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_products_sku", columnNames = "sku"))
public class Product extends BaseModel {
    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private int stockQuantity;

    @Column(length = 2048)
    private String imageUrl;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_category"))
    private Category category;

    protected Product() {
    }

    public Product(String sku, String title, String description, BigDecimal price, String currency,
                   int stockQuantity, String imageUrl, Category category) {
        this.sku = sku;
        this.title = title;
        this.description = description;
        this.price = price;
        this.currency = currency;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public void reserve(int quantity) {
        if (quantity <= 0 || stockQuantity < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product " + getId());
        }
        stockQuantity -= quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Release quantity must be positive");
        }
        stockQuantity = Math.addExact(stockQuantity, quantity);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
