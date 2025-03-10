package com.system.account_service.controllers;

import com.system.account_service.dtos.interest_rate.InterestRateDTO;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.entities.InterestRates;
import com.system.account_service.services.InterestRateService;
import com.system.account_service.utils.LocaleUtils;
import com.system.account_service.utils.MessageKeys;
import com.system.account_service.utils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts/interest-rate")
@RequiredArgsConstructor
@Tag(name = "Interest Rate API")
public class InterestRateController {
    private final InterestRateService service;
    private final WebUtils webUtils;
    private final LocaleUtils localeUtils;


    @GetMapping("")
    @Operation(summary = "Get interest rate list pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "application/json", schema =
                    @Schema(implementation = PageDataDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> getPagination(
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            WebRequest request
    ) {
        PageDataDTO<InterestRates> pageData = service.findAll(page, pageSize);
        HttpStatus status = HttpStatus.OK;
        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_GET_SUCCESS, request);

        return ResponseEntity
                .status(status)
                .body(webUtils.buildApiResponse(status, msg, pageData));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Interest rate by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "application/json", schema =
                    @Schema(implementation = InterestRates.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(schema = @Schema(implementation = String.class))})
    })
    public ResponseEntity<?> getById(
            @PathVariable(name = "id") String id,
            WebRequest request
    ) {
        InterestRates data = service.findById(id);
        HttpStatus status = HttpStatus.OK;
        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_GET_SUCCESS, request);

        return ResponseEntity
                .status(status)
                .body(webUtils.buildApiResponse(status, msg, data));
    }

    @PostMapping("")
    @Operation(summary = "Create new Interest rate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "403", description = "Access Denied", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> create(
            @RequestBody @Valid InterestRateDTO data,
            WebRequest request
    ) {
        InterestRates createdData = service.create(data);
        HttpStatus status = HttpStatus.CREATED;
        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_CREATED, request);

        return ResponseEntity
                .status(status)
                .body(webUtils.buildApiResponse(status, msg, createdData));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Interest rate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "403", description = "Access Denied", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> update(
            @PathVariable String id,
            @RequestBody @Valid InterestRateDTO data,
            WebRequest request
    ) {
        InterestRates updatedData = service.update(id, data);
        HttpStatus status = HttpStatus.OK;
        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_CREATED, request);

        return ResponseEntity
                .status(status)
                .body(webUtils.buildApiResponse(status, msg, updatedData));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Interest rate")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Fail", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "403", description = "Access Denied", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> delete(
            @PathVariable(name = "id") String id,
            WebRequest request
    ) {
        service.delete(id);
        HttpStatus status = HttpStatus.NO_CONTENT;
        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_DELETED, request);

        return ResponseEntity
                .status(status)
                .body(webUtils.buildApiResponse(status, msg, null));
    }

    @DeleteMapping("")
    @Operation(summary = "Delete Interest rate by Ids")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "400", description = "Fail", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "403", description = "Access Denied", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<?> deleteByIds(
            @RequestBody List<String> ids,
            WebRequest request
    ) {
        service.deleteIds(ids);
        HttpStatus status = HttpStatus.NO_CONTENT;
        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_DELETED, request);

        return ResponseEntity
                .status(status)
                .body(webUtils.buildApiResponse(status, msg, null));
    }
}
