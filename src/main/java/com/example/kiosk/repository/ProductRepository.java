package com.example.kiosk.repository;

import com.example.kiosk.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndRestaurantId(Long id, Long restaurantId);
}