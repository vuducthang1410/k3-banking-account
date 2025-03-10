package com.system.account_service.utils;

import com.system.account_service.dtos.response.ApiResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;

@Component
public class WebUtils {
    public WebRequest getCurrentRequest() {
        return (WebRequest) RequestContextHolder.currentRequestAttributes();
    }

    public <T> ApiResponseDTO<T> buildApiResponse(HttpStatus status, String message, T data) {
        return ApiResponseDTO.<T>builder()
                .code(status.value())
                .message(message)
                .data(data)
                .build();
    }
}
