package com.system.api_gateway.config;

import com.system.api_gateway.filter.JwtAuthenticationFilter;
import com.system.api_gateway.util.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity(proxyTargetClass = true)
public class SecurityConfig {

    private final MessageSource messageSource;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] unauthenticatedRequest = new String[]{
            "/swagger/**",
            "/swagger-ui/**",
            "/actuator/**",
            "/v3/api-docs/**",
            "/*/v3/api-docs",
            "/*/api/v1/**"
    };

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        http.cors(ServerHttpSecurity.CorsSpec::disable).csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers(unauthenticatedRequest).permitAll()
                .anyExchange().authenticated());

        http.addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()));

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
}
