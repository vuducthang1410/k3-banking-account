package org.demo.loanservice.controllers.exception;

import com.system.common_library.exception.DubboException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.common.MessageData;
import org.demo.loanservice.common.MessageValue;
import org.demo.loanservice.common.Util;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Hidden
public class GlobalExceptionHandler {
    private final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);
    private final Util util;

    /**
     * Handles validation exceptions for method arguments.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        // Log the validation error
        logger.error("Validation error in method argument: {}", ex.getMessage());

        // Collect validation error messages
        List<String> errorList = ex.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        // Return response with validation error messages
        return createdResponse(errorList,
                util.getMessageFromMessageSource(MessageData.INVALID_DATA.getKeyMessage()),
                MessageValue.STATUS_CODE_BAD_REQUEST,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing request parameters in the servlet request.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerMissingParameterException(
            MissingServletRequestParameterException ex) {

        // Log the missing parameter error
        logger.error("Missing request parameter: {}", ex.getParameterName());

        // Return response for missing parameter
        return createdResponse(util.getMessageFromMessageSource(MessageData.MISSING_PARAMETER.getKeyMessage()) + ex.getParameterName(),
                util.getMessageFromMessageSource(MessageData.MISSING_PARAMETER.getKeyMessage()),
                MessageValue.STATUS_CODE_BAD_REQUEST,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles missing request headers in the servlet request.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerMissingRequestHeaderException(
            MissingRequestHeaderException ex) {

        // Log the missing header error
        logger.error("Missing request header: {}", ex.getParameter().getParameterName());

        // Return response for missing header
        return createdResponse(util.getMessageFromMessageSource(MessageData.MISSING_PARAMETER_IN_HEADER.getKeyMessage()) + ex.getParameter().getParameterName(),
                util.getMessageFromMessageSource(MessageData.MISSING_PARAMETER.getKeyMessage()),
                MessageValue.STATUS_CODE_BAD_REQUEST,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles cases where data is not found in the system.
     */
    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerInDataNotFoundException(
            DataNotFoundException ex) {

        // Log the data not found error
        logger.error("Data not found: {}", ex.getMessageKey());

        // Return response for data not found
        return createdResponse(util.getMessageFromMessageSource(ex.getMessageKey()),
                util.getMessageFromMessageSource(MessageData.DATA_NOT_FOUND.getKeyMessage()),
                ex.getCode(),
                HttpStatus.NOT_FOUND);
    }

    /**
     * Handles cases where the provided data is not valid.
     */
    @ExceptionHandler(DataNotValidException.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerInDataNotValidException(
            DataNotValidException ex) {

        // Log the data validation error
        logger.error("Invalid data: {}", ex.getMessageKey());

        // Return response for invalid data
        return createdResponse(util.getMessageFromMessageSource(ex.getMessageKey()),
                util.getMessageFromMessageSource(MessageData.INVALID_DATA.getKeyMessage()),
                ex.getCode(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataNotValidWithConditionException.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerDataNotValidWithConditionException(
            DataNotValidWithConditionException ex) {
        String messageResponse = util.getMessageTransactionFromMessageSource(ex.getMessageKey(), ex.getCondition());
        // Log the data validation error
        logger.error(messageResponse);

        // Return response for invalid data
        return createdResponse(messageResponse,
                util.getMessageFromMessageSource(MessageData.INVALID_DATA.getKeyMessage()),
                ex.getCode(),
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles server errors in the application.
     */
    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerServerErrorException(
            ServerErrorException serverErrorException) {

        // Log the server error
        logger.error("Server error: {}", serverErrorException.getMessage());

        // Return response for server error
        return createdResponse(util.getMessageFromMessageSource(MessageData.SERVER_ERROR.getKeyMessage()),
                MessageData.SERVER_ERROR.getCode()
        );
    }

    /**
     * Handles method validation errors from handler methods.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerHandlerMethodValidationException(
            HandlerMethodValidationException ex) {

        // Log the validation method error
        logger.error("Method validation error: {}", ex.getMessage());
        // Collect validation error messages
        Set<String> errors = ex.getAllErrors().stream().map(MessageSourceResolvable::getDefaultMessage).collect(Collectors.toSet());
        // Return response with validation error messages
        return createdResponse(errors, util.getMessageFromMessageSource(MessageData.INVALID_DATA.getKeyMessage()), "40000", HttpStatus.BAD_REQUEST);
    }

    /**
     * General exception handler to catch any other unforeseen exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerException(Exception ex) {
        // Log the general exception
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        // Return response for unexpected error
        return createdResponse(util.getMessageFromMessageSource(MessageData.SERVER_ERROR.getKeyMessage()),
                MessageValue.STATUS_CODE_SERVER_ERROR
        );
    }

    @ExceptionHandler(DubboException.class)
    public ResponseEntity<DataResponseWrapper<Object>> handlerDubboException(DubboException ex) {
        // Log the general exception
        logger.error("Dubbo error: {}", ex.getMessage(), ex);
        // Return response for unexpected error
        return createdResponse(util.getMessageFromMessageSource(MessageData.SERVER_ERROR.getKeyMessage()),
                MessageValue.DUBBO_SERVICE_ERROR
        );
    }

    /**
     * @param body       The body of the response, can be an error message or data.
     * @param message    The message for the response.
     * @param status     The status code for the response.
     * @param httpStatus The HTTP status code.
     */
    private ResponseEntity<DataResponseWrapper<Object>> createdResponse(Object body, String message, String status, HttpStatus httpStatus) {
        return new ResponseEntity<>(new DataResponseWrapper<>(body, message, status), httpStatus);
    }

    private ResponseEntity<DataResponseWrapper<Object>> createdResponse(String message, String status) {
        return new ResponseEntity<>(new DataResponseWrapper<>(null, message, status), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
