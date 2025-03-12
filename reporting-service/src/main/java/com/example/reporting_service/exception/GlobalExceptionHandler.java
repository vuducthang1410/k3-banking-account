package com.example.reporting_service.exception;

import com.alibaba.dubbo.rpc.RpcException;
import com.example.reporting_service.model.dto.ApiResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    // Lỗi hệ thống chung
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseWrapper<Object> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error.system.unknown");
    }

    // Lỗi RPC
    @ExceptionHandler(RpcException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiResponseWrapper<Object> handleRpcException(RpcException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "error.rpc.failed");
    }

    // Lỗi validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseWrapper<Object> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::resolveFieldErrorMessage)
                .collect(Collectors.toList());

        return new ApiResponseWrapper<>(HttpStatus.BAD_REQUEST.value(), getMessage("error.validation.failed"), errors);
    }

    // Helper: Lấy thông báo lỗi từ `messages.properties`
    private String getMessage(String code) {
        return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
    }

    // Helper: Xử lý lỗi của từng field
    private String resolveFieldErrorMessage(FieldError error) {
        return error.getField() + ": " + getMessage(error.getDefaultMessage());
    }

    // Helper: Tạo response đơn giản
    private ApiResponseWrapper<Object> buildResponse(HttpStatus status, String code) {
        return new ApiResponseWrapper<>(status.value(), getMessage(code), null);
    }
}
