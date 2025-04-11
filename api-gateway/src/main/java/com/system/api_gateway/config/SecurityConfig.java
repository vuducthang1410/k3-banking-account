package com.system.api_gateway.config;

import com.system.api_gateway.util.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity(proxyTargetClass = true)
public class SecurityConfig {

    private final MessageSource messageSource;

    private static final String[] unauthenticatedRequest = new String[]{
            "/swagger/**",
            "/swagger-ui/**",
            "/actuator/**",
            "/v3/api-docs/**",
            "/*/v3/api-docs",
            "/customer/api/v1/users/create",
            "/customer/api/v1/auth/*",
            "/customer/api/v1/auth/token",
            "/customer/api/v1/users/get-all-province",
            "/customer/api/v1/auth/verify"
    };

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http.cors(ServerHttpSecurity.CorsSpec::disable).csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.authorizeExchange(exchange ->
                exchange.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(unauthenticatedRequest).permitAll()
                        .anyExchange().authenticated()
        ).oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }

    private ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, e) -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
            response.getHeaders().set("message", "Unauthorized");
            return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(
                            messageSource.getMessage(Constant.UNAUTHORIZED, null, LocaleContextHolder.getLocale())
                                    .getBytes(StandardCharsets.UTF_8)
                    ))
            );
        };
    }

    private ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, e) -> {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
            response.getHeaders().set("message", "Access Denied");
            return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(
                            messageSource.getMessage(Constant.FORBIDDEN, null, LocaleContextHolder.getLocale())
                                    .getBytes(StandardCharsets.UTF_8)
                    ))
            );
        };
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder.withJwkSetUri("http://localhost:8180/realms/klb/protocol/openid-connect/certs")
                .build();
    }
}
