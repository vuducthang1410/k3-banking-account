package com.system.napas_service.advice;

import com.system.napas_service.util.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApplicationExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).contentType(MediaType.TEXT_PLAIN)
                .body(messageSource.getMessage(Constant.PAYLOAD_TOO_LARGE, null, LocaleContextHolder.getLocale()));
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    public ResponseEntity<?> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).contentType(MediaType.TEXT_PLAIN)
                .body(messageSource.getMessage(Constant.UNSUPPORTED_MEDIA_TYPE, null, LocaleContextHolder.getLocale()));
    }

    @ExceptionHandler({TimeoutException.class})
    public ResponseEntity<?> handleTimeoutException(TimeoutException ex) {

        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).contentType(MediaType.TEXT_PLAIN)
                .body(messageSource.getMessage(Constant.REQUEST_TIMEOUT, null, LocaleContextHolder.getLocale()));
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<?> handleInvalidParameterException(InvalidParameterException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN)
                .body(messageSource.getMessage(Constant.INVALID_PARAMETER, null, LocaleContextHolder.getLocale()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN)
                .body(messageSource.getMessage(Constant.INTERNAL_SERVER_ERROR, null, LocaleContextHolder.getLocale()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN)
                .body(messageSource.getMessage(Constant.INVALID_PARAMETER, null, LocaleContextHolder.getLocale()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        Map<String, Set<String>> errorMap = new HashMap<>();
        BindingResult bindingResult = ex.getBindingResult();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String fieldName = fieldError.getField();
            String errorMessage = fieldError.getDefaultMessage();

            errorMap.computeIfAbsent(fieldName, k -> new HashSet<>()).add(errorMessage);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body(errorMap);
    }
}
