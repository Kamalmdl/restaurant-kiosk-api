package com.example.kiosk;

import com.example.kiosk.domain.Product;
import com.example.kiosk.domain.Restaurant;
import com.example.kiosk.repository.ProductRepository;
import com.example.kiosk.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * İlk işəsalma zamanı demo restoranları və məhsulları əlavə edir.
 * Əgər verilənlər bazasında artıq restoranlar varsa, heç nə dəyişdirilmir.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final RestaurantRepository restaurantRepository;
    private final ProductRepository productRepository;

    public DataSeeder(RestaurantRepository restaurantRepository, ProductRepository productRepository) {
        this.restaurantRepository = restaurantRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (restaurantRepository.count() > 0) {
            log.info("Seed data skipped: database already contains restaurants.");
            return;
        }

        Restaurant burgerHouse = restaurantRepository.save(new Restaurant("Burger House"));
        Restaurant pizzaPalace = restaurantRepository.save(new Restaurant("Pizza Palace"));
        Restaurant sushiCorner = restaurantRepository.save(new Restaurant("Sushi Corner"));

        productRepository.saveAll(List.of(
                new Product(burgerHouse, "Cheeseburger", "Burger", new BigDecimal("8.50")),
                new Product(burgerHouse, "French Fries", "Side", new BigDecimal("3.20")),
                new Product(burgerHouse, "Cola", "Drink", new BigDecimal("2.00")),

                new Product(pizzaPalace, "Margherita", "Pizza", new BigDecimal("11.00")),
                new Product(pizzaPalace, "Pepperoni", "Pizza", new BigDecimal("13.50")),
                new Product(pizzaPalace, "Lemonade", "Drink", new BigDecimal("2.50")),

                new Product(sushiCorner, "Salmon Roll", "Sushi", new BigDecimal("9.90")),
                new Product(sushiCorner, "Miso Soup", "Soup", new BigDecimal("3.80")),
                new Product(sushiCorner, "Green Tea", "Drink", new BigDecimal("2.20"))
        ));

        log.info("Seed data created: {} restaurants, {} products.",
                restaurantRepository.count(), productRepository.count());
    }
}