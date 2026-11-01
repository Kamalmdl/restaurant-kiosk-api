package com.example.kiosk.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;


@Schema(description = "Data of the new product")
public record CreateProductRequest(

        @Schema(description = "Product name", example = "Ice Tea")
        @NotBlank(message = "name must not be blank")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @Schema(description = "Product category", example = "Drink")
        @NotBlank(message = "category must not be blank")
        @Size(max = 50, message = "category must be at most 50 characters")
        String category,

        @Schema(description = "Unit price, greater than 0, at most 2 decimal places", example = "2.50")
        @NotNull(message = "price is required")
        @DecimalMin(value = "0.00", inclusive = false, message = "price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "price must have at most 2 decimal places")
        BigDecimal price
) {
}