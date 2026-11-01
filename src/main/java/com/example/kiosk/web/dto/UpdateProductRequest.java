package com.example.kiosk.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

/**
 * Request body for a partial update. Both fields are optional:
 * a field that is null (not sent) is left unchanged.
 */
public record UpdateProductRequest(

        @DecimalMin(value = "0.00", inclusive = false, message = "price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "price must have at most 2 decimal places")
        BigDecimal price,

        Boolean available
) {
}