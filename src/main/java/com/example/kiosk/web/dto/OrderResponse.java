package com.example.kiosk.web.dto;

import com.example.kiosk.domain.Order;
import com.example.kiosk.domain.OrderLine;
import com.example.kiosk.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long restaurantId,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<OrderLineResponse> lines
) {

    public static OrderResponse from(Order order) {
        List<OrderLineResponse> lines = order.getLines().stream()
                .map(OrderLineResponse::from)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getRestaurant().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                lines
        );
    }

    public record OrderLineResponse(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {

        public static OrderLineResponse from(OrderLine line) {
            return new OrderLineResponse(
                    line.getProduct().getId(),
                    line.getProductName(),
                    line.getQuantity(),
                    line.getUnitPrice(),
                    line.getLineTotal()
            );
        }
    }
}