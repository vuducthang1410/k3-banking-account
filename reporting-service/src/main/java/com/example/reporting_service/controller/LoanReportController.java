package com.example.reporting_service.controller;

import com.example.reporting_service.model.dto.LoansFilterRequest;
import com.example.reporting_service.model.dto.PersonalLoanRequest;
import com.example.reporting_service.service.LoanReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loan")
public class LoanReportController {

    @Autowired
    private LoanReportService loanReportService;

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> getLoanReport(@RequestBody PersonalLoanRequest request) throws Exception {
        byte[] pdfContent = loanReportService.createLoanReport(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=loan_report.pdf");
        headers.add("Content-Type", "application/pdf");
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    @PostMapping("/list-pdf")
    public ResponseEntity<byte[]> getLoansReportByFilter(@RequestBody LoansFilterRequest request) throws Exception {
        byte[] pdfContent = loanReportService.createLoansReportByFilter(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=loans_report.pdf");
        headers.add("Content-Type", "application/pdf");
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

}
