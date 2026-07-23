package com.scaler.backendproject.integrations.fakestore;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "fakeStoreClient", url = "${app.integrations.fakestore.base-url}")
public interface FakeStoreClient {
    @GetMapping("/products")
    List<FakeStoreProduct> getProducts();
}
