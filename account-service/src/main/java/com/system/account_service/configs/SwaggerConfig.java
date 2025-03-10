package com.system.account_service.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(
                        description = "API Gateway",
                        url = "http://localhost:8080/account"
                ),
                @Server(
                        description = "Local Environment",
                        url = "http://localhost:8082"
                ),
        },
        info = @Info(
                contact = @Contact(
                        name = "DoNgocDong",
                        email = "ngocdong2110.2003@gmail.com",
                        url = "https://github.com/DoNgocDong"
                ),
                title = "MICRO ACCOUNT SERVICE OPEN API",
                description = "API Documentation for Micro AccountService project",
                version = "1.0"
        )
)
public class SwaggerConfig {
}
