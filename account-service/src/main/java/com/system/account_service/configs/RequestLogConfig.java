package com.system.account_service.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogConfig extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        }
        finally {
            long processTime = System.currentTimeMillis() - startTime;

            log.info("{} {} [{}] {} {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getContentType(),
                    response.getStatus(),
                    processTime);
        }
    }
}
