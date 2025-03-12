package com.example.reporting_service.controller;

import com.example.reporting_service.model.dto.ApiResponseWrapper;
import com.example.reporting_service.model.dto.TransactionsFilterRequest;
import com.example.reporting_service.service.TransactionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/v1")
public class TransactionReportController {

    @Autowired
    private TransactionReportService transactionReportService;

    @PostMapping("/transactions/pdf")
    public ResponseEntity<ApiResponseWrapper<String>> getTransactionsReportByFilter(@RequestBody TransactionsFilterRequest request) throws Exception {
        String fileUrl = transactionReportService.createTransactionsReportByFilter(request);
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(201, "Report generated successfully", fileUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
