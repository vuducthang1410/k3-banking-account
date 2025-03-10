package com.system.core_banking_service.config;

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
                        description = "Local Environment",
                        url = "http://localhost:8090"
                )
        })
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("\uD83D\uDCC0 Core Banking Service")
                        .version("1.0")
                        .description("Core Banking Services (CBS) refer to the essential banking functions provided by" +
                                " financial institutions to manage customer accounts and transactions.")
                        .contact(new Contact()
                                .name("thinhpham2k2")
                                .email("thinhphamquoc9999@gmail.com")));
    }
}
