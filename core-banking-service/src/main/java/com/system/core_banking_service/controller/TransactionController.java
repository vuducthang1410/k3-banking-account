package com.system.core_banking_service.controller;

import com.system.common_library.dto.request.transaction.CoreTransactionRollbackDTO;
import com.system.common_library.dto.request.transaction.CreateExternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateInternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateSystemTransactionDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.common_library.exception.GroupValidationException;
import com.system.core_banking_service.service.interfaces.TransactionService;
import com.system.core_banking_service.util.Constant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequiredArgsConstructor
@Tag(name = "\uD83D\uDCB1 Transaction API")
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final MessageSource messageSource;

    private final TransactionService transactionService;

    @PostMapping("/external")
    @Operation(summary = "Create external transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content =
                    {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionCoreNapasDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> createExternal(@RequestBody @Validated CreateExternalTransactionDTO create)
            throws MethodArgumentTypeMismatchException {

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                .body(transactionService.createExternal(create));
    }

    @PostMapping("/internal")
    @Operation(summary = "Create internal transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content =
                    {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionCoreNapasDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> createInternal(@RequestBody @Validated CreateInternalTransactionDTO create)
            throws MethodArgumentTypeMismatchException {

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                .body(transactionService.createInternal(create));
    }

    @PostMapping("/system")
    @Operation(summary = "Create system transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content =
                    {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionCoreNapasDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> createSystem(@RequestBody @Validated CreateSystemTransactionDTO create)
            throws MethodArgumentTypeMismatchException {

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                .body(transactionService.createSystem(create));
    }

    @PostMapping("/rollback")
    @Operation(summary = "Rollback transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> rollback(@RequestBody @Validated CoreTransactionRollbackDTO rollback)
            throws MethodArgumentTypeMismatchException, GroupValidationException {

        transactionService.rollback(rollback);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN)
                .body(messageSource.getMessage(Constant.ROLLBACK_SUCCESS, null, LocaleContextHolder.getLocale()));
    }
}
