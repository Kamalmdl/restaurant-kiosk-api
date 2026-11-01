package com.example.kiosk;

import com.example.kiosk.domain.Product;
import com.example.kiosk.domain.Restaurant;
import com.example.kiosk.repository.ProductRepository;
import com.example.kiosk.repository.RestaurantRepository;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Bütün API testləri üçün ortaq tənzimləmələr və köməkçi vasitələr.
 * Tam Spring konteksti bir dəfə işə salınır və hər bir alt sinif tərəfindən yenidən istifadə olunur.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTestBase {

    protected static final String PRODUCTS = "/api/restaurants/{restaurantId}/products";
    protected static final String ORDERS = "/api/restaurants/{restaurantId}/orders";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestaurantRepository restaurantRepository;

    @Autowired
    protected ProductRepository productRepository;

    /** Tamamilə yeni bir restoran yaradır ki, bir test digər testlərlə heç vaxt məlumat paylaşmasın. */
    protected Restaurant newRestaurant() {
        return restaurantRepository.save(new Restaurant("Test Restaurant " + UUID.randomUUID()));
    }

    protected Product newProduct(Restaurant restaurant, String name, String price) {
        return productRepository.save(new Product(restaurant, name, "Test", new BigDecimal(price)));
    }

    /** (Məhsul ID-si, miqdar) cütlüklərindən JSON gövdəsi yaradır: items(1, 2, 5, 1) -> iki məhsul. */
    protected static String items(long... productIdAndQuantity) {
        StringBuilder json = new StringBuilder("{\"items\":[");
        for (int i = 0; i < productIdAndQuantity.length; i += 2) {
            if (i > 0) {
                json.append(",");
            }
            json.append("{\"productId\":").append(productIdAndQuantity[i])
                    .append(",\"quantity\":").append(productIdAndQuantity[i + 1]).append("}");
        }
        return json.append("]}").toString();
    }

    protected ResultActions postOrder(long restaurantId, String body) throws Exception {
        return mockMvc.perform(post(ORDERS, restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    protected long createOrder(long restaurantId, String body) throws Exception {
        String response = postOrder(restaurantId, body)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    protected ResultActions changeStatus(long restaurantId, long orderId, String newStatus) throws Exception {
        return mockMvc.perform(patch(ORDERS + "/{orderId}/status", restaurantId, orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + newStatus + "\"}"));
    }

    protected ResultActions patchProduct(long restaurantId, long productId, String body) throws Exception {
        return mockMvc.perform(patch(PRODUCTS + "/{productId}", restaurantId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}