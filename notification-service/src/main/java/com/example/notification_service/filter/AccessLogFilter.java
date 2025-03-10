package com.example.notification_service.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class AccessLogFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AccessLogFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String clientIP = httpRequest.getRemoteAddr();

        long startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
        long duration = System.currentTimeMillis() - startTime;

        int status = httpResponse.getStatus();

        StringBuilder requestBody = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpRequest.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        logger.info("Client IP: {}, Method: {}, URI: {}, Status: {}, Duration: {} ms",
                clientIP, method, uri, status, duration);
    }
}
