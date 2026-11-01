package com.example.kiosk.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Order request sent by the kiosk")
public record CreateOrderRequest(

        @Schema(description = "Ordered products. The same product sent twice is merged into one line.")
        @NotEmpty(message = "items must contain at least one product")
        List<@NotNull(message = "item must not be null") @Valid OrderItemRequest> items
) {

    @Schema(description = "One ordered product")
    public record OrderItemRequest(

            @Schema(description = "ID of a product that belongs to this restaurant and is on sale", example = "1")
            @NotNull(message = "productId is required")
            Long productId,

            @Schema(description = "Quantity, from 1 to 100", example = "2")
            @NotNull(message = "quantity is required")
            @Min(value = 1, message = "quantity must be at least 1")
            @Max(value = 100, message = "quantity must be at most 100")
            Integer quantity
    ) {
    }
}