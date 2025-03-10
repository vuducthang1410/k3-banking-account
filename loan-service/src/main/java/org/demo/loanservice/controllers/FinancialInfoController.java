package org.demo.loanservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.common.Util;
import org.demo.loanservice.dto.request.ApproveFinancialInfoRq;
import org.demo.loanservice.dto.request.FinancialInfoRq;
import org.demo.loanservice.services.IFinancialInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(Util.API_RESOURCE + "/financial-info")
public class FinancialInfoController {
    private final IFinancialInfoService financialInfoService;

    @Operation(
            summary = "Save financial information of an individual customer",
            description = "This API allows an individual customer to submit financial information along with income verification documents."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully saved financial info",
                    content = @Content(schema = @Schema(implementation = DataResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/individual-customer/save-info")
    public ResponseEntity<DataResponseWrapper<Object>> saveInfo(
            @RequestPart(name = "incomeVerificationDocuments")
            @Parameter(description = "List of income verification documents")
            List<MultipartFile> incomeVerificationDocuments,
            @Valid
            @RequestPart(name = "financialInfoRq")
            @Parameter(description = "Financial information request body")
            FinancialInfoRq financialInfoRq,

            @RequestHeader(name = "transactionId")
            @Parameter(description = "Unique transaction ID for tracking requests")
            String transactionId
    ) {
        return ResponseEntity.ok(financialInfoService.saveInfoIndividualCustomer(
                financialInfoRq, incomeVerificationDocuments, transactionId));
    }

    @Operation(
            summary = "Get all financial information by status",
            description = "Retrieve a paginated list of financial records based on their status (default: PENDING)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved data"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/individual-customer/get-all-info-by-status")
    public ResponseEntity<DataResponseWrapper<Object>> getAllInfoByStatus(
            @RequestHeader(name = "transactionId")
            @Parameter(description = "Unique transaction ID")
            String transactionId,

            @RequestParam(name = "pageNumber", defaultValue = "0", required = false)
            @Parameter(description = "Page number for pagination (default: 0)")
            Integer pageNumber,

            @RequestParam(name = "pageSize", defaultValue = "12", required = false)
            @Parameter(description = "Number of items per page (default: 12)")
            Integer pageSize,

            @RequestParam(name = "status", defaultValue = "PENDING", required = false)
            @Parameter(description = "Status of financial records to fetch")
            String status
    ) {
        return ResponseEntity.ok(financialInfoService.getAllInfoIsByStatus(pageNumber, pageSize, status, transactionId));
    }

    @Operation(
            summary = "Get detailed financial information by ID",
            description = "Fetch detailed information of a specific financial record using its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved detail information"),
            @ApiResponse(responseCode = "404", description = "Financial info not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/individual-customer/get-detail-info/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> getDetailInfo(
            @PathVariable(name = "id")
            @Parameter(description = "Unique ID of the financial record")
            String id,

            @RequestHeader(name = "transactionId")
            @Parameter(description = "Unique transaction ID")
            String transactionId
    ) {
        return ResponseEntity.ok(financialInfoService.getDetailInfoById(id, transactionId));
    }

    @Operation(
            summary = "Approve financial information",
            description = "Approve a pending financial record by providing necessary details."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully approved financial info"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/individual-customer/financial-info/approve")
    public ResponseEntity<DataResponseWrapper<Object>> approveFinancialInfo(
            @Valid
            @RequestBody
            @Parameter(description = "Request body containing approval details")
            ApproveFinancialInfoRq approveFinancialInfoRq,

            @RequestHeader(name = "transactionId")
            @Parameter(description = "Unique transaction ID")
            String transactionId
    ) {
        return ResponseEntity.ok(financialInfoService.approveFinancialInfo(approveFinancialInfoRq, transactionId));
    }

    @Operation(
            summary = "Verify financial information",
            description = "Verify the financial details of an individual customer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully verified financial info"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/individual-customer/financial-info/verify")
    public ResponseEntity<DataResponseWrapper<Object>> verifyFinancialInfo(
            @RequestHeader(name = "transactionId")
            @Parameter(description = "Unique transaction ID")
            String transactionId,
            @RequestParam(name = "customerId") String customerId
    ) {
        return ResponseEntity.ok(financialInfoService.verifyFinancialInfo(transactionId, customerId));
    }
    @GetMapping("/individual-customer/financial-info/get-info")
    public ResponseEntity<DataResponseWrapper<Object>> getFinancialInfo(
            @RequestHeader(name = "transactionId")String transactionId,
            @RequestParam(name = "cifCode")String cifCode
    ){
        return ResponseEntity.ok(financialInfoService.getFinancialInfoByCifCode(cifCode,transactionId));
    }
}
