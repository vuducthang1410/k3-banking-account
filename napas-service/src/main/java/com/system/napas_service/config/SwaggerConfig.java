package com.system.napas_service.config;

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
                        url = "http://localhost:8091"
                )
        })
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("\uD83E\uDD1D Napas Service")
                        .version("1.0")
                        .description("Napas Service is a leading electronic payment service provider in Vietnam that " +
                                "facilitates secure and efficient financial transactions.")
                        .contact(new Contact()
                                .name("thinhpham2k2")
                                .email("thinhphamquoc9999@gmail.com")));
    }
}
