package com.system.napas_service.controller;

import com.system.napas_service.dto.bank.BankDTO;
import com.system.napas_service.dto.bank.BankExtraDTO;
import com.system.napas_service.dto.bank.CreateBankDTO;
import com.system.napas_service.dto.bank.UpdateBankDTO;
import com.system.napas_service.dto.response.PagedDTO;
import com.system.napas_service.service.interfaces.BankService;
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
@Tag(name = "\uD83C\uDFE6 Bank API")
@RequestMapping("/api/v1/banks")
public class BankController {

    private final MessageSource messageSource;

    private final BankService bankService;

    @GetMapping("")
    @Operation(summary = "Get bank list")
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
                                     @RequestParam(defaultValue = "") Boolean isAvailable,
                                     @RequestParam(defaultValue = "id,desc") String sort,
                                     @RequestParam(defaultValue = "0") Integer page,
                                     @RequestParam(defaultValue = "10") Integer limit)
            throws MethodArgumentTypeMismatchException {

        PagedDTO<BankDTO> list = bankService.findAllByCondition(isAvailable, search, sort, page, limit);

        if (!list.getContent().isEmpty()) {

            return ResponseEntity.status(HttpStatus.OK).body(list);
        } else {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_PLAIN).body(
                    messageSource.getMessage(Constant.NOT_FOUND, null, LocaleContextHolder.getLocale()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bank by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = BankExtraDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "403", description = "Access Denied", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> getById(@PathVariable(value = "id") String id)
            throws MethodArgumentTypeMismatchException {

        BankExtraDTO bank = bankService.findById(id);

        if (bank != null) {

            return ResponseEntity.status(HttpStatus.OK).body(bank);
        } else {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.TEXT_PLAIN).body(
                    messageSource.getMessage(Constant.NOT_FOUND, null, LocaleContextHolder.getLocale()));
        }
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create bank")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "413", description = "Payload Too Large", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> create(@ModelAttribute @Validated CreateBankDTO create)
            throws MethodArgumentTypeMismatchException {

        bankService.create(create);

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.TEXT_PLAIN).body(
                messageSource.getMessage(Constant.CREATE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update bank")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "413", description = "Payload Too Large", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> update(@PathVariable(value = "id") String id,
                                    @ModelAttribute @Validated UpdateBankDTO update)
            throws MethodArgumentTypeMismatchException {

        bankService.update(update, id);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(
                messageSource.getMessage(Constant.UPDATE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bank")
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

        bankService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).contentType(MediaType.TEXT_PLAIN).body(
                messageSource.getMessage(Constant.DELETE_SUCCESS, null, LocaleContextHolder.getLocale()));
    }
}
