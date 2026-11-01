package com.example.kiosk.web.dto;

import java.math.BigDecimal;

/** One entry of the "top selling products" ranking. */
public record TopProductResponse(
        Long productId,
        String productName,
        Long totalQuantity,
        BigDecimal totalRevenue
) {
}