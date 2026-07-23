package com.scaler.backendproject.service;

import com.scaler.backendproject.dto.CreateOrderRequest;
import com.scaler.backendproject.exceptions.ConflictException;
import com.scaler.backendproject.models.Product;
import com.scaler.backendproject.repository.OrderRepository;
import com.scaler.backendproject.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, productRepository, new OrderMapper());
        when(orderRepository.findByIdempotencyKeyAndCustomerEmail(any(), any()))
                .thenReturn(Optional.empty());
        lenient().when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void mergesDuplicateLinesAndCalculatesTotalOnServer() {
        Product product = product("p1", "USD", new BigDecimal("25.00"));
        when(productRepository.findAllActiveByIdForUpdate(List.of("p1"))).thenReturn(List.of(product));

        var response = orderService.create("buyer@example.com", "idem-1",
                new CreateOrderRequest(List.of(
                        new CreateOrderRequest.Item("p1", 1),
                        new CreateOrderRequest.Item("p1", 2)
                )));

        assertThat(response.totalAmount()).isEqualByComparingTo("75.00");
        assertThat(response.items()).singleElement().extracting(item -> item.quantity()).isEqualTo(3);
        verify(product).reserve(3);
    }

    @Test
    void rejectsOrderWhenStockReservationFails() {
        Product product = product("p1", "USD", new BigDecimal("25.00"));
        doThrow(new IllegalArgumentException("insufficient")).when(product).reserve(5);
        when(productRepository.findAllActiveByIdForUpdate(List.of("p1"))).thenReturn(List.of(product));

        assertThatThrownBy(() -> orderService.create("buyer@example.com", "idem-2",
                new CreateOrderRequest(List.of(new CreateOrderRequest.Item("p1", 5)))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Insufficient stock");
    }

    private Product product(String id, String currency, BigDecimal price) {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(id);
        when(product.getSku()).thenReturn("SKU-" + id);
        when(product.getTitle()).thenReturn("Product " + id);
        when(product.getCurrency()).thenReturn(currency);
        when(product.getPrice()).thenReturn(price);
        return product;
    }
}
