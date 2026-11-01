package com.example.kiosk;

import com.example.kiosk.domain.Product;
import com.example.kiosk.domain.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductApiTest extends ApiTestBase {

    @Test
    void createdProductIsAvailableByDefault() throws Exception {
        Restaurant restaurant = newRestaurant();

        mockMvc.perform(post(PRODUCTS, restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ice Tea\",\"category\":\"Drink\",\"price\":2.5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ice Tea"))
                .andExpect(jsonPath("$.price").value(2.5))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void invalidBodyReturns400WithFieldErrors() throws Exception {
        Restaurant restaurant = newRestaurant();

        mockMvc.perform(post(PRODUCTS, restaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"category\":\"Drink\",\"price\":-5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    @Test
    void creatingProductForUnknownRestaurantReturns404() throws Exception {
        mockMvc.perform(post(PRODUCTS, 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ice Tea\",\"category\":\"Drink\",\"price\":2.5}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateChangesPriceAndAvailabilityIndependently() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product product = newProduct(restaurant, "Burger", "8.50");

        patchProduct(restaurant.getId(), product.getId(), "{\"price\":9.99}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.available").value(true));


        patchProduct(restaurant.getId(), product.getId(), "{\"available\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.available").value(false));

        patchProduct(restaurant.getId(), product.getId(), "{\"available\":true}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void emptyUpdateBodyReturns400() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product product = newProduct(restaurant, "Burger", "8.50");

        patchProduct(restaurant.getId(), product.getId(), "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannotUpdateProductOfAnotherRestaurant() throws Exception {
        Restaurant owner = newRestaurant();
        Restaurant other = newRestaurant();
        Product product = newProduct(owner, "Burger", "8.50");

        patchProduct(other.getId(), product.getId(), "{\"price\":1.00}")
                .andExpect(status().isNotFound());
    }
}