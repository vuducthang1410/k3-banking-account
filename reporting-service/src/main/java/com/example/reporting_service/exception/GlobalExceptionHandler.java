package com.example.reporting_service.exception;

import com.example.reporting_service.model.dto.ApiResponseWrapper;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleGeneralException(Exception ex) {
        String message = resolveErrorMessage(ex.getMessage(), "error.system.unknown");
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }

    @ExceptionHandler(RpcException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleRpcException(RpcException ex) {
        String message = resolveErrorMessage(ex.getMessage(), "error.rpc.failed");
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, message, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseWrapper<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::resolveFieldErrorMessage)
                .collect(Collectors.toList());

        String message = resolveErrorMessage(ex.getMessage(),"error.validation.failed");
        return buildResponse(HttpStatus.BAD_REQUEST, message, errors);
    }

    private String resolveErrorMessage(String code, String defaultCode) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, messageSource.getMessage(defaultCode, null, defaultCode, locale), locale);
    }

    private String resolveFieldErrorMessage(FieldError error) {
        return messageSource.getMessage(error.getDefaultMessage(), null, error.getDefaultMessage(), LocaleContextHolder.getLocale());
    }

    private ResponseEntity<ApiResponseWrapper<Object>> buildResponse(HttpStatus status, String message, Object data) {
        return ResponseEntity.status(status)
                .body(new ApiResponseWrapper<>(status.value(), message, data));
    }
}
