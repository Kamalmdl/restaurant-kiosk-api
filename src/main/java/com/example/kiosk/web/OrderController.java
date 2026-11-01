package com.example.kiosk.web;

import com.example.kiosk.domain.OrderStatus;
import com.example.kiosk.service.OrderService;
import com.example.kiosk.web.dto.CreateOrderRequest;
import com.example.kiosk.web.dto.OrderResponse;
import com.example.kiosk.web.dto.PagedResponse;
import com.example.kiosk.web.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@PathVariable Long restaurantId,
                                @Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(restaurantId, request);
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable Long restaurantId,
                                      @PathVariable Long orderId,
                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(restaurantId, orderId, request.status());
    }

    @GetMapping
    public PagedResponse<OrderResponse> list(@PathVariable Long restaurantId,
                                             @RequestParam(required = false) OrderStatus status,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return orderService.list(restaurantId, status, page, size);
    }
}