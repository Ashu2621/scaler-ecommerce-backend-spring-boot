package com.scaler.backendproject.controller;

import com.scaler.backendproject.service.FakeStoreImportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/catalog")
public class CatalogImportController {
    private final FakeStoreImportService importService;

    public CatalogImportController(FakeStoreImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/imports/fakestore")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Integer> importFakeStore() {
        return Map.of("imported", importService.importProducts());
    }
}
