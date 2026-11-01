package com.example.kiosk.web.dto;

import com.example.kiosk.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(

        @NotNull(message = "status is required")
        OrderStatus status
) {
}