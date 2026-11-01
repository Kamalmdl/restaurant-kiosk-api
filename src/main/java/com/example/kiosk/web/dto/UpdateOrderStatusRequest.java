package com.example.kiosk.web.dto;

import com.example.kiosk.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

/** Request body for changing the status of an order. */
public record UpdateOrderStatusRequest(

        @NotNull(message = "status is required")
        OrderStatus status
) {
}