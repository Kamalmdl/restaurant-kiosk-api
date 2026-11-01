package com.example.kiosk;

import com.example.kiosk.domain.Product;
import com.example.kiosk.domain.Restaurant;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TopSellingApiTest extends ApiTestBase {

    private static final String TOP_SELLING = PRODUCTS + "/top-selling";

    @Test
    void cancelledOrdersAreNotCounted() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product popular = newProduct(restaurant, "Popular", "5.00");
        Product cancelledOnly = newProduct(restaurant, "CancelledOnly", "1.00");

        createOrder(restaurant.getId(), items(popular.getId(), 3));
        long cancelledOrder = createOrder(restaurant.getId(), items(cancelledOnly.getId(), 50));
        changeStatus(restaurant.getId(), cancelledOrder, "CANCELLED").andExpect(status().isOk());

        mockMvc.perform(get(TOP_SELLING, restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productName").value("Popular"))
                .andExpect(jsonPath("$[0].totalQuantity").value(3));
    }

    @Test
    void returnsOnlyTopFiveSortedByQuantity() throws Exception {
        Restaurant restaurant = newRestaurant();

        for (int i = 1; i <= 6; i++) {
            Product product = newProduct(restaurant, "P" + i, "2.00");
            createOrder(restaurant.getId(), items(product.getId(), 7 - i));
        }

        mockMvc.perform(get(TOP_SELLING, restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].productName").value("P1"))
                .andExpect(jsonPath("$[0].totalQuantity").value(6))
                .andExpect(jsonPath("$[4].productName").value("P5"))
                .andExpect(jsonPath("$[4].totalQuantity").value(2))

                .andExpect(jsonPath("$[?(@.productName == 'P6')]").isEmpty());
    }

    @Test
    void ties_areBrokenByProductNameAndQuantitiesAreSummedAcrossOrders() throws Exception {
        Restaurant restaurant = newRestaurant();

        Product banana = newProduct(restaurant, "Banana", "1.00");
        Product apple = newProduct(restaurant, "Apple", "2.00");

        createOrder(restaurant.getId(), items(banana.getId(), 3));
        createOrder(restaurant.getId(), items(apple.getId(), 1));
        createOrder(restaurant.getId(), items(apple.getId(), 2));   // Apple total: 1 + 2 = 3

        mockMvc.perform(get(TOP_SELLING, restaurant.getId()))
                .andExpect(jsonPath("$[0].productName").value("Apple"))
                .andExpect(jsonPath("$[0].totalQuantity").value(3))
                .andExpect(jsonPath("$[0].totalRevenue").value(6.0))
                .andExpect(jsonPath("$[1].productName").value("Banana"));
    }

    @Test
    void salesOfOtherRestaurantsAreNotMixedIn() throws Exception {
        Restaurant restaurant = newRestaurant();
        Restaurant other = newRestaurant();
        Product otherProduct = newProduct(other, "OtherDish", "4.00");
        createOrder(other.getId(), items(otherProduct.getId(), 9));

        mockMvc.perform(get(TOP_SELLING, restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void unknownRestaurantReturns404() throws Exception {
        mockMvc.perform(get(TOP_SELLING, 999999))
                .andExpect(status().isNotFound());
    }
}