package com.scaler.backendproject.service;

import com.scaler.backendproject.dto.CategoryRequest;
import com.scaler.backendproject.dto.CategoryResponse;
import com.scaler.backendproject.exceptions.ConflictException;
import com.scaler.backendproject.models.Category;
import com.scaler.backendproject.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final SlugService slugService;
    private final CatalogMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, SlugService slugService, CatalogMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.slugService = slugService;
        this.mapper = mapper;
    }

    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        String slug = slugService.slugify(name);
        if (categoryRepository.existsByNameIgnoreCase(name) || categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("CATEGORY_EXISTS", "Category already exists: " + name);
        }
        return mapper.toResponse(categoryRepository.save(new Category(name, slug)));
    }

    @Transactional(readOnly = true)
    @Cacheable("categories")
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .filter(category -> !category.isDeleted())
                .map(mapper::toResponse)
                .toList();
    }
}
