package com.system.napas_service.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.system.napas_service.util.Constant;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ContentType;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessLogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        if (requestWrapper.getRequestURI().toLowerCase().contains("/api/v1")) {

            long time = System.currentTimeMillis();
            try {

                filterChain.doFilter(requestWrapper, responseWrapper);
            } finally {

                time = System.currentTimeMillis() - time;
                String remoteIpAddress = requestWrapper.getHeader("X-FORWARDED-FOR");
                if (remoteIpAddress == null || remoteIpAddress.isEmpty()) {

                    remoteIpAddress = requestWrapper.getRemoteAddr();
                }

                String requestContentType = Optional.ofNullable(requestWrapper.getContentType()).orElse("");
                String responseContentType = Optional.ofNullable(responseWrapper.getContentType()).orElse("");
                String parameter = getParameter(requestWrapper.getParameterMap());
                String requestBody = requestContentType.startsWith(ContentType.MULTIPART_FORM_DATA.getMimeType())
                        ? parameter :
                        new String(requestWrapper.getContentAsByteArray(), requestWrapper.getCharacterEncoding());
                String responseBody = requestWrapper.getMethod().equalsIgnoreCase(HttpMethod.GET.toString())
                        ? Constant.BLANK :
                        new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());

                if (!responseBody.isBlank()) {

                    try {

                        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                        Object json = gson.fromJson(responseBody, Object.class);
                        responseBody = gson.toJson(json);
                    } catch (Exception ignored) {

                    }
                }

                log.info("""
                                
                                . Client IP: {}
                                . Method: {}
                                . Path: {}
                                . Parameters: {}
                                . Status code: {}
                                . Time: {}ms
                                . Request body (Content type: {}):
                                {}
                                . Response body (Content type: {}):
                                {}""",
                        remoteIpAddress, requestWrapper.getMethod(), requestWrapper.getRequestURI(), parameter,
                        responseWrapper.getStatus(), time, requestContentType, requestBody, responseContentType,
                        responseBody);
                requestWrapper.getInputStream();
                responseWrapper.copyBodyToResponse();
            }
        } else {

            filterChain.doFilter(requestWrapper, responseWrapper);
            requestWrapper.getInputStream();
            responseWrapper.copyBodyToResponse();
        }
    }

    private String getParameter(Map<String, String[]> map) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {

            String key = entry.getKey();
            String[] values = entry.getValue();
            for (String value : values) {

                sb.append(key).append("=").append(value).append(", ");
            }
        }

        return sb.lastIndexOf(",") < 1 ? sb.toString() : sb.substring(0, sb.lastIndexOf(","));
    }
}
