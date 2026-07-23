package com.scaler.backendproject.service;

import com.scaler.backendproject.dto.CreateOrderRequest;
import com.scaler.backendproject.dto.OrderResponse;
import com.scaler.backendproject.dto.PageResponse;
import com.scaler.backendproject.exceptions.BadRequestException;
import com.scaler.backendproject.exceptions.ConflictException;
import com.scaler.backendproject.exceptions.NotFoundException;
import com.scaler.backendproject.models.CustomerOrder;
import com.scaler.backendproject.models.Product;
import com.scaler.backendproject.repository.OrderRepository;
import com.scaler.backendproject.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper mapper;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        OrderMapper mapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    @Transactional
    public OrderResponse create(String customerEmail,
                                String idempotencyKey,
                                CreateOrderRequest request) {
        validateIdempotencyKey(idempotencyKey);
        var existing = orderRepository.findByIdempotencyKeyAndCustomerEmail(idempotencyKey, customerEmail);
        if (existing.isPresent()) {
            return mapper.toResponse(existing.get());
        }

        Map<String, Integer> requested = request.items().stream()
                .collect(Collectors.toMap(
                        CreateOrderRequest.Item::productId,
                        CreateOrderRequest.Item::quantity,
                        Integer::sum,
                        LinkedHashMap::new));
        List<String> sortedIds = requested.keySet().stream().sorted().toList();
        Map<String, Product> products = productRepository.findAllActiveByIdForUpdate(sortedIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        if (products.size() != requested.size()) {
            String missing = sortedIds.stream().filter(id -> !products.containsKey(id)).findFirst().orElse("unknown");
            throw new NotFoundException("Product", missing);
        }

        String currency = products.values().iterator().next().getCurrency();
        if (products.values().stream().anyMatch(product -> !currency.equals(product.getCurrency()))) {
            throw new BadRequestException("MIXED_CURRENCY", "All order items must use the same currency");
        }

        CustomerOrder order = new CustomerOrder(customerEmail, idempotencyKey, currency);
        for (String productId : sortedIds) {
            Product product = products.get(productId);
            int quantity = requested.get(productId);
            try {
                product.reserve(quantity);
            } catch (IllegalArgumentException exception) {
                throw new ConflictException("INSUFFICIENT_STOCK",
                        "Insufficient stock for product " + productId);
            }
            order.addItem(product, quantity);
        }
        productRepository.saveAll(products.values());
        return mapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse findOwned(String customerEmail, String orderId) {
        return mapper.toResponse(orderRepository.findOwnedById(orderId, customerEmail)
                .orElseThrow(() -> new NotFoundException("Order", orderId)));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> findMine(String customerEmail, int page, int size) {
        return PageResponse.from(
                orderRepository.findByCustomerEmailOrderByCreatedAtDesc(
                        customerEmail, PageRequest.of(page, size)),
                mapper::toResponse
        );
    }

    private void validateIdempotencyKey(String value) {
        if (value == null || value.isBlank() || value.length() > 100) {
            throw new BadRequestException("INVALID_IDEMPOTENCY_KEY",
                    "Idempotency-Key must contain 1 to 100 characters");
        }
    }
}
