package com.example.notification_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(
                        description = "API Gateway",
                        url = "http://localhost:8080/notification"
                ),
                @Server(
                        description = "Local Environment",
                        url = "http://localhost:8086"
                )
        }
)

public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI();
    }
}
