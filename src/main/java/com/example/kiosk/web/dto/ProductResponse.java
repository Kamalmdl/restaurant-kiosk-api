package com.example.kiosk.web.dto;

import com.example.kiosk.domain.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long restaurantId,
        String name,
        String category,
        BigDecimal price,
        boolean available
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getRestaurant().getId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.isAvailable()
        );
    }
}