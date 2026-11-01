package com.example.kiosk.service;

import com.example.kiosk.domain.Order;
import com.example.kiosk.domain.OrderLine;
import com.example.kiosk.domain.OrderStatus;
import com.example.kiosk.domain.Product;
import com.example.kiosk.domain.Restaurant;
import com.example.kiosk.exception.BadRequestException;
import com.example.kiosk.exception.ConflictException;
import com.example.kiosk.exception.NotFoundException;
import com.example.kiosk.repository.OrderRepository;
import com.example.kiosk.repository.ProductRepository;
import com.example.kiosk.repository.RestaurantRepository;
import com.example.kiosk.web.dto.CreateOrderRequest;
import com.example.kiosk.web.dto.OrderResponse;
import com.example.kiosk.web.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderService {

    private static final int MAX_PAGE_SIZE = 100;

    private final RestaurantRepository restaurantRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderService(RestaurantRepository restaurantRepository,
                        ProductRepository productRepository,
                        OrderRepository orderRepository) {
        this.restaurantRepository = restaurantRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }


    @Transactional
    public OrderResponse create(Long restaurantId, CreateOrderRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found: " + restaurantId));

        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (CreateOrderRequest.OrderItemRequest item : request.items()) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        Order order = new Order(restaurant);
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Long productId = entry.getKey();

            Product product = productRepository.findByIdAndRestaurantId(productId, restaurantId)
                    .orElseThrow(() -> new BadRequestException(
                            "Product " + productId + " does not exist in restaurant " + restaurantId));

            if (!product.isAvailable()) {
                throw new BadRequestException("Product " + productId + " is not available");
            }

            OrderLine line = new OrderLine(product, entry.getValue());
            order.addLine(line);
            total = total.add(line.getLineTotal());
        }

        order.setTotalAmount(total);


        return OrderResponse.from(orderRepository.save(order));
    }


    @Transactional
    public OrderResponse updateStatus(Long restaurantId, Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> new NotFoundException(
                        "Order " + orderId + " not found in restaurant " + restaurantId));

        OrderStatus current = order.getStatus();
        if (!current.canTransitionTo(newStatus)) {
            throw new ConflictException(
                    "Cannot change order status from " + current + " to " + newStatus);
        }


        order.setStatus(newStatus);
        return OrderResponse.from(order);
    }


    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> list(Long restaurantId, OrderStatus status, int page, int size) {
        if (page < 0) {
            throw new BadRequestException("page must be 0 or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant not found: " + restaurantId);
        }


        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));

        Page<Order> orders = (status == null)
                ? orderRepository.findByRestaurantId(restaurantId, pageable)
                : orderRepository.findByRestaurantIdAndStatus(restaurantId, status, pageable);

        return PagedResponse.from(orders.map(OrderResponse::from));
    }
}