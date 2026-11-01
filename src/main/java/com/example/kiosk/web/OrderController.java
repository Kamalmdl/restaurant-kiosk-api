package com.example.kiosk.web;

import com.example.kiosk.domain.OrderStatus;
import com.example.kiosk.exception.ApiError;
import com.example.kiosk.service.OrderService;
import com.example.kiosk.web.dto.CreateOrderRequest;
import com.example.kiosk.web.dto.OrderResponse;
import com.example.kiosk.web.dto.PagedResponse;
import com.example.kiosk.web.dto.UpdateOrderStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Orders", description = "Create orders (kiosk) and process them (kitchen)")
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create an order",
            description = "Creates a PENDING order. Prices are copied from the products and the total is "
                    + "calculated by the server. Duplicate products are merged into one line.")
    @ApiResponse(responseCode = "201", description = "Order created")
    @ApiResponse(responseCode = "400",
            description = "Validation failed, or a product does not exist in this restaurant, or is not available",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Restaurant not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(
            @Parameter(description = "Restaurant ID (seed data: 1, 2 or 3)", example = "1")
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateOrderRequest request) {
        return orderService.create(restaurantId, request);
    }


    @Operation(summary = "Change order status",
            description = "Allowed flow: PENDING -> PREPARING -> READY -> COMPLETED. "
                    + "CANCELLED is possible only from PENDING. COMPLETED and CANCELLED are final.")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "Missing or unknown status value",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Order not found in this restaurant",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "This status change is not allowed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(
            @Parameter(description = "Restaurant ID", example = "1") @PathVariable Long restaurantId,
            @Parameter(description = "Order ID", example = "1") @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(restaurantId, orderId, request.status());
    }

    @Operation(summary = "List orders",
            description = "Orders of the restaurant, newest first, with optional status filter and pagination. "
                    + "Each order contains its lines and total amount.")
    @ApiResponse(responseCode = "200", description = "Page of orders returned")
    @ApiResponse(responseCode = "400", description = "Invalid status, page or size",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Restaurant not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping
    public PagedResponse<OrderResponse> list(
            @Parameter(description = "Restaurant ID", example = "1") @PathVariable Long restaurantId,
            @Parameter(description = "Optional status filter (upper case)") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Page number, starts at 0", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size, from 1 to 100", example = "20") @RequestParam(defaultValue = "20") int size) {
        return orderService.list(restaurantId, status, page, size);
    }
}