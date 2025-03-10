package org.demo.loanservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.common.Util;
import org.demo.loanservice.dto.request.InterestRateRq;
import org.demo.loanservice.services.IInterestRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Util.API_RESOURCE + "/interest-rate")
@RequiredArgsConstructor
public class InterestRateController {
    private final IInterestRateService interestRateService;

    @Operation(
            summary = "Create new interest rate",
            description = "This API endpoint is used to create a new interest rate with the provided parameters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Interest rate created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input, bad request")
    })
    @PostMapping("/save")
    public ResponseEntity<DataResponseWrapper<Object>> save(
            @RequestBody @Valid InterestRateRq interestRateRq,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(interestRateService.save(interestRateRq, transactionId), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get interest rate by ID",
            description = "This API endpoint retrieves an interest rate by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest rate retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Interest rate not found for the given ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> getById(
            @Parameter(description = "ID of the interest rate to be retrieved") @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(interestRateService.getById(id, transactionId), HttpStatus.OK);
    }

    @Operation(
            summary = "Get all interest rates with pagination",
            description = "This API endpoint retrieves all interest rates with pagination support."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of interest rates retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/get-all-loan-product/{loanProductId}")
    public ResponseEntity<DataResponseWrapper<Object>> getAll(
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "12", required = false) Integer pageSize,
            @PathVariable(name = "loanProductId") String loanProductId,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(interestRateService.getAllInterestRateByLoanProductId(pageNumber, pageSize, loanProductId, transactionId), HttpStatus.OK);
    }

    @Operation(
            summary = "Activate an interest rate",
            description = "This API endpoint is used to activate an interest rate using the provided ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest rate activated successfully"),
            @ApiResponse(responseCode = "404", description = "Interest rate not found for the given ID")
    })
    @PatchMapping("/active/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> active(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(interestRateService.active(id, transactionId), HttpStatus.OK);
    }

    @Operation(
            summary = "Update an interest rate",
            description = "This API endpoint is used to update an interest rate with the provided ID and parameters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest rate updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Interest rate not found for the given ID")
    })
    @PatchMapping("/update/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> update(
            @Valid
            @RequestBody InterestRateRq interestRateRq,
            @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(interestRateService.update(id, interestRateRq, transactionId), HttpStatus.OK);
    }

    @Operation(
            summary = "Delete an interest rate",
            description = "This API endpoint is used to delete an interest rate using the provided ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Interest rate deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Interest rate not found for the given ID")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> delete(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(interestRateService.delete(id, transactionId), HttpStatus.OK);
    }
}
