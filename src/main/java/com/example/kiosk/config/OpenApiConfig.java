package com.example.kiosk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kioskOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Restaurant Kiosk API")
                .version("v1")
                .description("Simplified backend API: kiosks create orders, the kitchen prepares them. "
                        + "Seed restaurants: 1 = Burger House, 2 = Pizza Palace, 3 = Sushi Corner."));
    }
}