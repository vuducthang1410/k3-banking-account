package com.system.account_service.exception;

import com.system.account_service.dtos.response.ErrorResponseDTO;
import com.system.account_service.exception.payload.ExistedDataException;
import com.system.account_service.exception.payload.InvalidParamException;
import com.system.account_service.exception.payload.ResourceNotFoundException;
import com.system.account_service.utils.LocaleUtils;
import com.system.account_service.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ControllerAdvice
public class ApiExceptionHandler {
    private final LocaleUtils localeUtils;

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResourceFoundException(NoResourceFoundException e, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String message = localeUtils.getLocaleMsg(MessageKeys.PATH_NOT_FOUND, request);

        return buildResponseError(status, message, null, e);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String message = localeUtils.getLocaleMsg(MessageKeys.RESOURCE_NOT_FOUND_ERR, request);

        return buildResponseError(status, message, null, e);
    }

    @ExceptionHandler(InvalidParamException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidParamException(InvalidParamException e, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = localeUtils.getLocaleMsg(e.getMsgKey(), request);

        return buildResponseError(status, message, null, e);
    }

    @ExceptionHandler(ExistedDataException.class)
    public ResponseEntity<ErrorResponseDTO> handleExistedDataException(ExistedDataException e, WebRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        String message = localeUtils.getLocaleMsg(e.getMsgKey(), request);

        return buildResponseError(status, message, null, e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = localeUtils.getLocaleMsg(MessageKeys.REQUEST_BODY_INVALID, request);

        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + localeUtils.getLocaleMsg(err.getDefaultMessage(), request))
                .toList();

        return buildResponseError(status, message, errors, e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception e, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = localeUtils.getLocaleMsg(MessageKeys.INTERNAL_SERVER_ERR, request);

        return buildResponseError(status, message, null, e);
    }

    private ResponseEntity<ErrorResponseDTO> buildResponseError(HttpStatus status, String message, List<String> errors, Exception e) {
        ErrorResponseDTO res = ErrorResponseDTO
                .builder()
                .code(status.value())
                .message(message)
                .errors( errors )
                .build();

        if(HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            log.error(e.getMessage(), e);
        }

        return ResponseEntity.status(status).body(res);
    }
}
