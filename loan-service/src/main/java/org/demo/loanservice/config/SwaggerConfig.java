
package org.demo.loanservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(servers = {@Server(description = "API Gateway", url = "http://localhost:8080/loan"), @Server(description = "Local Environment", url = "http://localhost:8084")})
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("\uD83D\uDCB8 Loan Service").version("1.0").description(""));
    }
}