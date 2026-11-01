package com.example.kiosk.repository;

import com.example.kiosk.domain.Order;
import com.example.kiosk.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {


    Optional<Order> findByIdAndRestaurantId(Long id, Long restaurantId);


    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);


    Page<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status, Pageable pageable);
}