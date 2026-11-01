package com.example.kiosk.service;

import com.example.kiosk.domain.Product;
import com.example.kiosk.domain.Restaurant;
import com.example.kiosk.exception.BadRequestException;
import com.example.kiosk.exception.NotFoundException;
import com.example.kiosk.repository.OrderLineRepository;
import com.example.kiosk.repository.ProductRepository;
import com.example.kiosk.repository.RestaurantRepository;
import com.example.kiosk.web.dto.CreateProductRequest;
import com.example.kiosk.web.dto.ProductResponse;
import com.example.kiosk.web.dto.TopProductResponse;
import com.example.kiosk.web.dto.UpdateProductRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProductService {

    private static final int TOP_SELLING_LIMIT = 5;

    private final RestaurantRepository restaurantRepository;
    private final ProductRepository productRepository;
    private final OrderLineRepository orderLineRepository;

    public ProductService(RestaurantRepository restaurantRepository,
                          ProductRepository productRepository,
                          OrderLineRepository orderLineRepository) {
        this.restaurantRepository = restaurantRepository;
        this.productRepository = productRepository;
        this.orderLineRepository = orderLineRepository;
    }


    @Transactional
    public ProductResponse create(Long restaurantId, CreateProductRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantId));

        Product product = new Product(
                restaurant,
                request.name().trim(),
                request.category().trim(),
                normalize(request.price())
        );


        return ProductResponse.from(productRepository.save(product));
    }


    @Transactional
    public ProductResponse update(Long restaurantId, Long productId, UpdateProductRequest request) {
        if (request.price() == null && request.available() == null) {
            throw new BadRequestException("Provide at least one field to update: price or available");
        }

        Product product = productRepository.findByIdAndRestaurantId(productId, restaurantId)
                .orElseThrow(() -> new NotFoundException(
                        "Product " + productId + " not found in restaurant " + restaurantId));

        if (request.price() != null) {
            product.setPrice(normalize(request.price()));
        }
        if (request.available() != null) {
            product.setAvailable(request.available());
        }


        return ProductResponse.from(product);
    }


    @Transactional(readOnly = true)
    public List<TopProductResponse> topSelling(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant not found: " + restaurantId);
        }
        return orderLineRepository.findTopSelling(restaurantId, PageRequest.of(0, TOP_SELLING_LIMIT));
    }


    private BigDecimal normalize(BigDecimal price) {
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}