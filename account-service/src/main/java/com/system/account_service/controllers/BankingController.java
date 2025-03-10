package com.system.account_service.controllers;

import com.system.account_service.dtos.banking.BankingRp;
import com.system.account_service.dtos.response.PageDataDTO;
import com.system.account_service.services.BankingAccountService;
import com.system.account_service.utils.LocaleUtils;
import com.system.account_service.utils.MessageKeys;
import com.system.account_service.utils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

@RestController
@RequestMapping("/api/v1/accounts/banking")
@RequiredArgsConstructor
@Tag(name = "Banking Account API")
public class BankingController {
    private final BankingAccountService service;
    private final WebUtils webUtils;
    private final LocaleUtils localeUtils;


    @GetMapping("")
    @Operation(summary = "Get Banking account list pagination")
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
        PageDataDTO<BankingRp> pageData = service.findAll(page, pageSize);
        HttpStatus status = HttpStatus.OK;
        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_GET_SUCCESS, request);

        return ResponseEntity
                .status(status)
                .body(webUtils.buildApiResponse(status, msg, pageData));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Banking account by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "application/json", schema =
                    @Schema(implementation = BankingRp.class))}),
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
        BankingRp data = service.findById(id);
        HttpStatus status = HttpStatus.OK;
        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_GET_SUCCESS, request);

        return ResponseEntity
                .status(status)
                .body(webUtils.buildApiResponse(status, msg, data));
    }

//    @PostMapping("")
//    @Operation(summary = "Create Banking account info")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Created", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//            @ApiResponse(responseCode = "400", description = "Bad Request", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//            @ApiResponse(responseCode = "401", description = "Unauthorized", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//            @ApiResponse(responseCode = "403", description = "Access Denied", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//    })
//    public ResponseEntity<?> create(
//            @RequestBody @Valid CreateBankingDTO data,
//            WebRequest request
//    ) {
//        BankingRp createdData = service.create(data);
//        HttpStatus status = HttpStatus.CREATED;
//        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_CREATED, request);
//
//        return ResponseEntity
//                .status(status)
//                .body(webUtils.buildApiResponse(status, msg, createdData));
//    }

//    @DeleteMapping("/{id}")
//    @Operation(summary = "Delete Banking account")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "204", description = "No Content", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//            @ApiResponse(responseCode = "400", description = "Fail", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//            @ApiResponse(responseCode = "401", description = "Unauthorized", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//            @ApiResponse(responseCode = "403", description = "Access Denied", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
//                    {@Content(schema = @Schema(implementation = String.class))}),
//    })
//    public ResponseEntity<?> delete(
//            @PathVariable(name = "id") String id,
//            WebRequest request
//    ) {
//        service.delete(id);
//        HttpStatus status = HttpStatus.NO_CONTENT;
//        String msg = localeUtils.getLocaleMsg(MessageKeys.DATA_DELETED, request);
//
//        return ResponseEntity
//                .status(status)
//                .body(webUtils.buildApiResponse(status, msg, null));
//    }
}
