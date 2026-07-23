package com.scaler.backendproject.service;

import com.scaler.backendproject.integrations.fakestore.FakeStoreClient;
import com.scaler.backendproject.integrations.fakestore.FakeStoreProduct;
import com.scaler.backendproject.models.Category;
import com.scaler.backendproject.models.Product;
import com.scaler.backendproject.repository.CategoryRepository;
import com.scaler.backendproject.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import com.scaler.backendproject.exceptions.ApiException;

@Service
public class FakeStoreImportService {
    private final Semaphore importSlots = new Semaphore(2, true);
    private final FakeStoreClient client;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SlugService slugService;

    public FakeStoreImportService(FakeStoreClient client,
                                  CategoryRepository categoryRepository,
                                  ProductRepository productRepository,
                                  SlugService slugService) {
        this.client = client;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.slugService = slugService;
    }

    @Transactional
    public int importProducts() {
        if (!importSlots.tryAcquire()) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "IMPORT_BUSY",
                    "Too many catalog imports are already running");
        }
        try {
            List<FakeStoreProduct> products = client.getProducts();
            for (FakeStoreProduct external : products) {
                String categoryName = external.category().trim();
                Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                        .orElseGet(() -> categoryRepository.save(
                                new Category(categoryName, slugService.slugify(categoryName))));
                String sku = "FAKE-" + external.id();
                Product product = productRepository.findBySkuIgnoreCase(sku)
                        .orElseGet(() -> new Product(
                                sku,
                                external.title(),
                                external.description(),
                                external.price(),
                                "USD",
                                100,
                                external.image(),
                                category
                        ));
                product.setTitle(external.title().trim());
                product.setDescription(external.description().trim());
                product.setPrice(external.price());
                product.setCurrency("USD".toUpperCase(Locale.ROOT));
                product.setImageUrl(external.image());
                product.setCategory(category);
                product.setActive(true);
                productRepository.save(product);
            }
            return products.size();
        } finally {
            importSlots.release();
        }
    }
}
