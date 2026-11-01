package com.example.kiosk.web.dto;

import java.math.BigDecimal;

public record TopProductResponse(
        Long productId,
        String productName,
        Long totalQuantity,
        BigDecimal totalRevenue
) {
}