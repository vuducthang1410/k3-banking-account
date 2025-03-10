package com.system.napas_service.controller;

import com.system.common_library.dto.response.account.AccountExtraNapasDTO;
import com.system.common_library.dto.response.account.AccountNapasDTO;
import com.system.napas_service.dto.account.AccountDTO;
import com.system.napas_service.dto.account.CreateAccountDTO;
import com.system.napas_service.dto.account.UpdateAccountDTO;
import com.system.napas_service.dto.response.PagedDTO;
import com.system.napas_service.service.interfaces.AccountService;
import com.system.napas_service.util.Constant;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequiredArgsConstructor
@Tag(name = "\uD83D\uDCB3 Account API")
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final MessageSource messageSource;

    private final AccountService accountService;

    @GetMapping("")
    @Operation(summary = "Get account list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = PagedDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> getList(@RequestParam(defaultValue = "") String search,
                                     @RequestParam(defaultValue = "") Boolean isActive,
                                     @RequestParam(defaultValue = "id,desc") String sort,
                                     @RequestParam(defaultValue = "0") Integer page,
                                     @RequestParam(defaultValue = "10") Integer limit)
            throws MethodArgumentTypeMismatchException {

        PagedDTO<AccountNapasDTO> list = accountService.findAllByCondition(isActive, search, sort, page, limit);

        if (!list.getContent().isEmpty()) {

            return ResponseEntity.status(HttpStatus.OK).body(list);
        } else {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_PLAIN).body(
                    messageSource.getMessage(Constant.NOT_FOUND, null, LocaleContextHolder.getLocale()));
        }
    }

    @GetMapping("/{account}")
    @Operation(summary = "Get account by account number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> getByAccountNumber(@PathVariable(value = "account") String account)
            throws MethodArgumentTypeMismatchException {

        AccountExtraNapasDTO ac = accountService.findByAccountNumber(account);

        if (ac != null) {

            return ResponseEntity.status(HttpStatus.OK).body(ac);
        } else {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_PLAIN).body(
                    messageSource.getMessage(Constant.NOT_FOUND, null, LocaleContextHolder.getLocale()));
        }
    }

    @PostMapping(value = "")
    @Operation(summary = "Create account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content =
                    {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> create(@RequestBody @Validated CreateAccountDTO create)
            throws MethodArgumentTypeMismatchException {

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                .body(accountService.create(create));
    }

    @PutMapping("/{account}")
    @Operation(summary = "Update account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> update(@PathVariable(value = "account") String account,
                                    @RequestBody @Validated UpdateAccountDTO update)
            throws MethodArgumentTypeMismatchException {

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                .body(accountService.update(update, account));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Fail", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> delete(@PathVariable(value = "id") String id)
            throws MethodArgumentTypeMismatchException {

        accountService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).contentType(MediaType.TEXT_PLAIN).body(
                messageSource.getMessage(Constant.DELETE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }
}
