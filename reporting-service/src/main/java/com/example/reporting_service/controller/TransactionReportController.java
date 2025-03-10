package com.example.reporting_service.controller;

import com.example.reporting_service.model.dto.TransactionsFilterRequest;
import com.example.reporting_service.service.TransactionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/v1/transaction")
public class TransactionReportController {

    @Autowired
    private TransactionReportService transactionReportService;

    @PostMapping("/list-pdf")
    public ResponseEntity<byte[]> getTransactionsReportByFilter(@RequestBody TransactionsFilterRequest request) throws Exception {
        byte[] pdfContent = transactionReportService.createTransactionsReportByFilter(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=transactions_report.pdf");
        headers.add("Content-Type", "application/pdf");
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
}
