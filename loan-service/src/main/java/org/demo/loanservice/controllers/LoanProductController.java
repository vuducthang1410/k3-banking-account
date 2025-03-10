package org.demo.loanservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.common.Util;
import org.demo.loanservice.dto.request.LoanProductRq;
import org.demo.loanservice.services.ILoanProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequiredArgsConstructor
@Schema
@RequestMapping(Util.API_RESOURCE + "/loan-product")
public class LoanProductController {
    private final ILoanProductService loanProductService;

    @Operation
    @GetMapping("/get-all-by-active")
    public ResponseEntity<DataResponseWrapper<Object>> getAll(
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false)
            @Schema(description = "Page number for pagination", example = "0")
            Integer pageNumber,

            @RequestParam(name = "pageSize", defaultValue = "12", required = false)
            @Schema(description = "Page size for pagination", example = "12")
            Integer pageSize,

            @RequestHeader(name = "transactionId")
            @Schema(description = "Unique transaction ID for the request", example = "12345abcde")
            String transactionId,
            @RequestParam(name = "active", defaultValue = "true") Boolean isActive
    ) {
        return new ResponseEntity<>(loanProductService.getAllByActive(pageNumber, pageSize, isActive, transactionId), HttpStatus.OK);
    }

    @Operation
    @GetMapping("/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> getById(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(loanProductService.getById(id, transactionId), HttpStatus.OK);
    }

    @Operation
    @PostMapping("/save")
    public ResponseEntity<DataResponseWrapper<Object>> save(
            @RequestBody @Valid LoanProductRq loanProductRq,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(loanProductService.save(loanProductRq, transactionId), HttpStatus.OK);
    }

    @Operation
    @PatchMapping("/active/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> active(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(loanProductService.active(id, transactionId), HttpStatus.OK);
    }

    @Operation
    @PutMapping("/update/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> update(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId,
            @RequestBody LoanProductRq loanProductRq
    ) {
        return new ResponseEntity<>(loanProductService.update(id, loanProductRq, transactionId), HttpStatus.OK);
    }

    @Operation
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> delete(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return new ResponseEntity<>(loanProductService.delete(id, transactionId), HttpStatus.OK);
    }

    @GetMapping("/get-all-loan-product-active")
    public ResponseEntity<DataResponseWrapper<Object>> getAllByActive(
            @RequestParam(name = "pageSize") Integer pageSize,
            @RequestParam(name = "pageNum") Integer pageNum,
            @RequestHeader(name = "transactionId") String transactionId
    ) {
        return ResponseEntity.ok(loanProductService.getAllLoanProductIsActive(pageNum, pageSize, transactionId));
    }

    @GetMapping("/loan-product-detail/{id}")
    public ResponseEntity<DataResponseWrapper<Object>> getLoanProductDetail(
            @PathVariable(name = "id") String id,
            @RequestHeader(name = "transactionId") String transactionId) {
        return ResponseEntity.ok(loanProductService.getLoanProductForUserById(id,transactionId));
    }
    @GetMapping("/loan-product/report")
    public ResponseEntity<DataResponseWrapper<Object>> getLoanProductReport(
            @RequestParam(name = "cifCode",required = false)String cifCode,
            @RequestParam(name = "fromDate",required = false)Date fromDate,
            @RequestParam(name = "endDate",required = false)Date endDate,
            @RequestHeader(name = "transactionId",required = false)String transactionId
            ){
        return ResponseEntity.ok(loanProductService.getLoanProductReport(cifCode,fromDate,endDate,transactionId));
    }
}
