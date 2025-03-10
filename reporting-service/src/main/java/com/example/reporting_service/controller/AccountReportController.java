package com.example.reporting_service.controller;

import com.example.reporting_service.model.dto.AccountsFilterRequest;
import com.example.reporting_service.model.dto.PersonalAccountRequest;
import com.example.reporting_service.service.AccountReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
public class AccountReportController {

    @Autowired
    private AccountReportService accountReportService;

    @PostMapping("/pdf")
    public ResponseEntity<byte[]> getAccountReport(@RequestBody PersonalAccountRequest request) throws Exception {
        byte[] pdfContent = accountReportService.createAccountReport(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=account_report.pdf");
        headers.add("Content-Type", "application/pdf");
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }


    @PostMapping("/list-pdf")
    public ResponseEntity<byte[]> getAccountsReportByFilter(@RequestBody AccountsFilterRequest request) throws Exception {
        byte[] pdfContent = accountReportService.createAccountsReportByFilter(request);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=account_report.pdf");
        headers.add("Content-Type", "application/pdf");
        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

}






