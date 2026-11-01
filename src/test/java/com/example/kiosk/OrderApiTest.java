package com.example.kiosk;

import com.example.kiosk.domain.Product;
import com.example.kiosk.domain.Restaurant;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderApiTest extends ApiTestBase {

    // ---------- orders ----------

    @Test
    void createOrderMergesDuplicateProductsAndCalculatesTotal() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product burger = newProduct(restaurant, "Burger", "8.50");
        Product fries = newProduct(restaurant, "Fries", "3.20");


        postOrder(restaurant.getId(), items(burger.getId(), 2, fries.getId(), 1, burger.getId(), 1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(28.7))
                .andExpect(jsonPath("$.lines.length()").value(2))
                .andExpect(jsonPath("$.lines[0].productName").value("Burger"))
                .andExpect(jsonPath("$.lines[0].quantity").value(3))
                .andExpect(jsonPath("$.lines[0].unitPrice").value(8.5))
                .andExpect(jsonPath("$.lines[0].lineTotal").value(25.5));
    }

    @Test
    void unavailableProductIsRejected() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product product = newProduct(restaurant, "Burger", "8.50");
        patchProduct(restaurant.getId(), product.getId(), "{\"available\":false}")
                .andExpect(status().isOk());

        postOrder(restaurant.getId(), items(product.getId(), 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void productOfAnotherRestaurantIsRejected() throws Exception {
        Restaurant restaurant = newRestaurant();
        Restaurant other = newRestaurant();
        Product foreignProduct = newProduct(other, "Pizza", "11.00");

        postOrder(restaurant.getId(), items(foreignProduct.getId(), 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidOrderBodiesReturn400() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product product = newProduct(restaurant, "Burger", "8.50");

        postOrder(restaurant.getId(), "{\"items\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.items").exists());

        postOrder(restaurant.getId(), items(product.getId(), 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors['items[0].quantity']").exists());
    }

    @Test
    void orderForUnknownRestaurantReturns404() throws Exception {
        postOrder(999999, items(1, 1)).andExpect(status().isNotFound());
    }

    @Test
    void laterPriceChangeDoesNotAffectExistingOrders() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product burger = newProduct(restaurant, "Burger", "8.50");
        long oldOrderId = createOrder(restaurant.getId(), items(burger.getId(), 1));

        patchProduct(restaurant.getId(), burger.getId(), "{\"price\":10.00}")
                .andExpect(status().isOk());
        createOrder(restaurant.getId(), items(burger.getId(), 1));


        mockMvc.perform(get(ORDERS, restaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].lines[0].unitPrice").value(10.0))
                .andExpect(jsonPath("$.content[1].id").value(oldOrderId))
                .andExpect(jsonPath("$.content[1].lines[0].unitPrice").value(8.5))
                .andExpect(jsonPath("$.content[1].totalAmount").value(8.5));
    }

    // ---------- status ----------

    @Test
    void orderMovesThroughTheFullLifecycle() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product product = newProduct(restaurant, "Burger", "8.50");
        long orderId = createOrder(restaurant.getId(), items(product.getId(), 1));

        changeStatus(restaurant.getId(), orderId, "PREPARING")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));
        changeStatus(restaurant.getId(), orderId, "READY")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"));
        changeStatus(restaurant.getId(), orderId, "COMPLETED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));


        changeStatus(restaurant.getId(), orderId, "PREPARING")
                .andExpect(status().isConflict());
    }

    @Test
    void skippingAStatusReturns409() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product product = newProduct(restaurant, "Burger", "8.50");
        long orderId = createOrder(restaurant.getId(), items(product.getId(), 1));

        changeStatus(restaurant.getId(), orderId, "READY")
                .andExpect(status().isConflict());
    }

    @Test
    void pendingOrderCanBeCancelledButNotAfterwards() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product product = newProduct(restaurant, "Burger", "8.50");

        long cancelledId = createOrder(restaurant.getId(), items(product.getId(), 1));
        changeStatus(restaurant.getId(), cancelledId, "CANCELLED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        changeStatus(restaurant.getId(), cancelledId, "PREPARING")
                .andExpect(status().isConflict());

        long preparingId = createOrder(restaurant.getId(), items(product.getId(), 1));
        changeStatus(restaurant.getId(), preparingId, "PREPARING").andExpect(status().isOk());
        changeStatus(restaurant.getId(), preparingId, "CANCELLED")
                .andExpect(status().isConflict());
    }

    @Test
    void unknownStatusValueReturns400AndUnknownOrderReturns404() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product product = newProduct(restaurant, "Burger", "8.50");
        long orderId = createOrder(restaurant.getId(), items(product.getId(), 1));

        changeStatus(restaurant.getId(), orderId, "WRONG").andExpect(status().isBadRequest());
        changeStatus(restaurant.getId(), 999999, "PREPARING").andExpect(status().isNotFound());
    }

    @Test
    void cannotChangeStatusOfAnotherRestaurantsOrder() throws Exception {
        Restaurant owner = newRestaurant();
        Restaurant other = newRestaurant();
        Product product = newProduct(owner, "Burger", "8.50");
        long orderId = createOrder(owner.getId(), items(product.getId(), 1));

        changeStatus(other.getId(), orderId, "PREPARING").andExpect(status().isNotFound());
    }

    // ---------- listing ----------

    @Test
    void listSupportsStatusFilterAndPagination() throws Exception {
        Restaurant restaurant = newRestaurant();
        Product product = newProduct(restaurant, "Burger", "8.50");

        long first = createOrder(restaurant.getId(), items(product.getId(), 1));
        long second = createOrder(restaurant.getId(), items(product.getId(), 1));
        long third = createOrder(restaurant.getId(), items(product.getId(), 1));
        changeStatus(restaurant.getId(), second, "PREPARING").andExpect(status().isOk());


        mockMvc.perform(get(ORDERS, restaurant.getId()).param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
        mockMvc.perform(get(ORDERS, restaurant.getId()).param("status", "PREPARING"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(second));


        mockMvc.perform(get(ORDERS, restaurant.getId()).param("page", "0").param("size", "2"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(third))
                .andExpect(jsonPath("$.content[1].id").value(second))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));


        mockMvc.perform(get(ORDERS, restaurant.getId()).param("page", "1").param("size", "2"))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(first));
    }

    @Test
    void listRejectsInvalidParametersAndUnknownRestaurant() throws Exception {
        Restaurant restaurant = newRestaurant();

        mockMvc.perform(get(ORDERS, restaurant.getId()).param("status", "WRONG"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(ORDERS, restaurant.getId()).param("size", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(ORDERS, restaurant.getId()).param("size", "101"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(ORDERS, restaurant.getId()).param("page", "-1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get(ORDERS, 999999))
                .andExpect(status().isNotFound());
    }
}