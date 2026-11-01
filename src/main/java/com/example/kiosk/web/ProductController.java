package com.example.kiosk.web;

import com.example.kiosk.exception.ApiError;
import com.example.kiosk.service.ProductService;
import com.example.kiosk.web.dto.CreateProductRequest;
import com.example.kiosk.web.dto.ProductResponse;
import com.example.kiosk.web.dto.TopProductResponse;
import com.example.kiosk.web.dto.UpdateProductRequest;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Products", description = "Manage the menu of a restaurant and see its best sellers")
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create a product",
            description = "Adds a new product to the restaurant. New products are on sale by default.")
    @ApiResponse(responseCode = "201", description = "Product created")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Restaurant not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
            @Parameter(description = "Restaurant ID (seed data: 1, 2 or 3)", example = "1")
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateProductRequest request) {
        return productService.create(restaurantId, request);
    }

    @Operation(summary = "Update a product",
            description = "Changes the price and/or the availability. Fields that are not sent stay unchanged. "
                    + "At least one field is required.")
    @ApiResponse(responseCode = "200", description = "Product updated")
    @ApiResponse(responseCode = "400", description = "Validation failed or no field was sent",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Product not found in this restaurant",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PatchMapping("/{productId}")
    public ProductResponse update(
            @Parameter(description = "Restaurant ID", example = "1") @PathVariable Long restaurantId,
            @Parameter(description = "Product ID", example = "1") @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request) {
        return productService.update(restaurantId, productId, request);
    }

    @Operation(summary = "Top 5 selling products",
            description = "Returns up to 5 products with the highest sold quantity. Cancelled orders are not counted. "
                    + "Ties are sorted by product name. Returns an empty list if nothing has been sold.")
    @ApiResponse(responseCode = "200", description = "Ranking returned")
    @ApiResponse(responseCode = "404", description = "Restaurant not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/top-selling")
    public List<TopProductResponse> topSelling(
            @Parameter(description = "Restaurant ID", example = "1") @PathVariable Long restaurantId) {
        return productService.topSelling(restaurantId);
    }
}