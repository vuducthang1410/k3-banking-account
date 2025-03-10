package com.system.api_gateway.config;

import com.system.api_gateway.swagger.SwaggerCodeBlockTransformer;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webflux.ui.SwaggerIndexTransformer;
import org.springdoc.webflux.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.web.filter.CharacterEncodingFilter;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        return Mono::justOrEmpty;
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    @Bean
    public SwaggerIndexTransformer swaggerIndexTransformer(
            SwaggerUiConfigProperties a,
            SwaggerUiOAuthProperties b,
            SwaggerWelcomeCommon c,
            ObjectMapperProvider d) {

        return new SwaggerCodeBlockTransformer(a, b, c, d);
    }
}
