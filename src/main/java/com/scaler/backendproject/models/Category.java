package com.scaler.backendproject.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_categories_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_categories_slug", columnNames = "slug")
        })
public class Category extends BaseModel {
    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 140)
    private String slug;

    @JsonIgnore
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    protected Category() {
    }

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<Product> getProducts() {
        return products;
    }
}
