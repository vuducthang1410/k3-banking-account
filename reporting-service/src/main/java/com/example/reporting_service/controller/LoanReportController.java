package com.example.reporting_service.controller;

import com.example.reporting_service.model.dto.ApiResponseWrapper;
import com.example.reporting_service.model.dto.LoansFilterRequest;
import com.example.reporting_service.model.dto.PersonalLoanRequest;
import com.example.reporting_service.service.LoanReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LoanReportController {

    @Autowired
    private LoanReportService loanReportService;

    @PostMapping("/loan/pdf")
    public ResponseEntity<ApiResponseWrapper<String>> getLoanReport(@RequestBody PersonalLoanRequest request) throws Exception {
        String fileUrl = loanReportService.createLoanReport(request);
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(201, "Loan report generated successfully", fileUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/loans/pdf")
    public ResponseEntity<ApiResponseWrapper<String>> getLoansReportByFilter(@RequestBody LoansFilterRequest request) throws Exception {
        String fileUrl = loanReportService.createLoansReportByFilter(request);
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(201, "Loans report generated successfully", fileUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
