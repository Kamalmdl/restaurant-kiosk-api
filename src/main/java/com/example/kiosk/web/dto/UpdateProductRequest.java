package com.example.kiosk.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;


@Schema(description = "Partial update: send only the fields you want to change (at least one)")
public record UpdateProductRequest(

        @Schema(description = "New unit price, greater than 0, at most 2 decimal places", example = "3.00")
        @DecimalMin(value = "0.00", inclusive = false, message = "price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "price must have at most 2 decimal places")
        BigDecimal price,

        @Schema(description = "false removes the product from sale, true puts it back", example = "false")
        Boolean available
) {
}