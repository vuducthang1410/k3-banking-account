package com.example.reporting_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(
                        description = "API Gateway",
                        url = "http://localhost:8080/report"
                ),
                @Server(
                        description = "Local Environment",
                        url = "http://localhost:8083"
                )
        }
)
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Learning Project API")
                        .version("1.0")
                        .description("This is the API documentation for the Learning Project")
                );
    }
}
