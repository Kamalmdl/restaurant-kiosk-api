package com.example.kiosk;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiDocsTest extends ApiTestBase {

    @Test
    void apiDocsContainAllSixOperations() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Restaurant Kiosk API"))
                .andExpect(jsonPath("$.paths['/api/restaurants/{restaurantId}/products'].post").exists())
                .andExpect(jsonPath("$.paths['/api/restaurants/{restaurantId}/products/{productId}'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/restaurants/{restaurantId}/products/top-selling'].get").exists())
                .andExpect(jsonPath("$.paths['/api/restaurants/{restaurantId}/orders'].post").exists())
                .andExpect(jsonPath("$.paths['/api/restaurants/{restaurantId}/orders'].get").exists())
                .andExpect(jsonPath("$.paths['/api/restaurants/{restaurantId}/orders/{orderId}/status'].patch").exists());
    }
}