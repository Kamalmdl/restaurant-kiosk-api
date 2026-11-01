package com.example.kiosk.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Request body for creating an order: a list of (productId, quantity) pairs. */
public record CreateOrderRequest(

        @NotEmpty(message = "items must contain at least one product")
        List<@NotNull(message = "item must not be null") @Valid OrderItemRequest> items
) {

    /** One requested product with its quantity. */
    public record OrderItemRequest(

            @NotNull(message = "productId is required")
            Long productId,

            @NotNull(message = "quantity is required")
            @Min(value = 1, message = "quantity must be at least 1")
            @Max(value = 100, message = "quantity must be at most 100")
            Integer quantity
    ) {
    }
}