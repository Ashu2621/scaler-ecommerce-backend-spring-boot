package com.scaler.backendproject.service;

import com.scaler.backendproject.dto.OrderResponse;
import com.scaler.backendproject.models.CustomerOrder;
import com.scaler.backendproject.models.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getItems().stream().map(this::toResponse).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderResponse.Item toResponse(OrderItem item) {
        return new OrderResponse.Item(
                item.getProductId(),
                item.getSku(),
                item.getProductTitle(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal()
        );
    }
}
