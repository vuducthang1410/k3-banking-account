package com.system.transaction_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(
                        description = "API Gateway",
                        url = "http://localhost:8080/transaction"
                ),
                @Server(
                        description = "Local Environment",
                        url = "http://localhost:8085"
                )
        })
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("\uD83D\uDCB8 Transaction Service")
                        .version("1.0")
                        .description("The Transaction Service in a banking application is responsible for managing " +
                                "financial transactions between accounts.")
                        .contact(new Contact()
                                .name("thinhpham2k2")
                                .email("thinhphamquoc9999@gmail.com")));
    }
}
