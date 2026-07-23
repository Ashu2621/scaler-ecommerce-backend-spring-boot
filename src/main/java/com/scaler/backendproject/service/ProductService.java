package com.scaler.backendproject.service;

import com.scaler.backendproject.dto.PageResponse;
import com.scaler.backendproject.dto.ProductRequest;
import com.scaler.backendproject.dto.ProductResponse;
import com.scaler.backendproject.dto.ProductUpdateRequest;
import com.scaler.backendproject.exceptions.ConflictException;
import com.scaler.backendproject.exceptions.NotFoundException;
import com.scaler.backendproject.models.Category;
import com.scaler.backendproject.models.Product;
import com.scaler.backendproject.repository.CategoryRepository;
import com.scaler.backendproject.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class ProductService {
    private static final Set<String> SORTABLE_FIELDS =
            Set.of("title", "price", "stockQuantity", "createdAt", "updatedAt");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CatalogMapper mapper;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          CatalogMapper mapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "products", key = "#id")
    public ProductResponse findById(String id) {
        return mapper.toResponse(productRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Product", id)));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String query,
                                                String category,
                                                int page,
                                                int size,
                                                String sort,
                                                Sort.Direction direction) {
        String sortField = SORTABLE_FIELDS.contains(sort) ? sort : "createdAt";
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Product> result = productRepository.searchActive(normalize(query), normalize(category), pageable);
        return PageResponse.from(result, mapper::toResponse);
    }

    @Transactional
    @CacheEvict(cacheNames = {"products", "categories"}, allEntries = true)
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new ConflictException("SKU_EXISTS", "Product SKU already exists: " + request.sku());
        }
        Category category = categoryRepository.findById(request.categoryId())
                .filter(item -> !item.isDeleted())
                .orElseThrow(() -> new NotFoundException("Category", request.categoryId()));
        Product product = new Product(
                request.sku().trim().toUpperCase(Locale.ROOT),
                request.title().trim(),
                request.description().trim(),
                request.price(),
                request.currency(),
                request.stockQuantity(),
                request.imageUrl(),
                category
        );
        return mapper.toResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public ProductResponse update(String id, ProductUpdateRequest request) {
        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
        if (request.title() != null) product.setTitle(request.title().trim());
        if (request.description() != null) product.setDescription(request.description().trim());
        if (request.price() != null) product.setPrice(request.price());
        if (request.currency() != null) product.setCurrency(request.currency());
        if (request.stockQuantity() != null) product.setStockQuantity(request.stockQuantity());
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());
        if (request.active() != null) product.setActive(request.active());
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .filter(item -> !item.isDeleted())
                    .orElseThrow(() -> new NotFoundException("Category", request.categoryId()));
            product.setCategory(category);
        }
        return mapper.toResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public void delete(String id) {
        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
        product.setActive(false);
        product.setDeleted(true);
        productRepository.save(product);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
