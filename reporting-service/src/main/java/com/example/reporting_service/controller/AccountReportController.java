package com.example.reporting_service.controller;

import com.example.reporting_service.model.dto.AccountsFilterRequest;
import com.example.reporting_service.model.dto.ApiResponseWrapper;
import com.example.reporting_service.model.dto.PersonalAccountRequest;
import com.example.reporting_service.service.AccountReportService;
import com.example.reporting_service.service.FilebaseStorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AccountReportController {

    @Autowired
    private AccountReportService accountReportService;

    @Autowired
    private FilebaseStorageService filebaseStorageService;

    @Autowired
    private MessageSource messageSource; // Thêm MessageSource

    @PostMapping("/account/pdf")
    public ResponseEntity<ApiResponseWrapper<String>> getAccountReport(@Valid @RequestBody PersonalAccountRequest request) throws Exception {
        String fileUrl = accountReportService.createAccountReport(request);
        String message = messageSource.getMessage("report.account.success", null, LocaleContextHolder.getLocale());
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(201, message, fileUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/accounts/pdf")
    public ResponseEntity<ApiResponseWrapper<String>> getAccountsReportByFilter(@Valid @RequestBody AccountsFilterRequest request) throws Exception {
        String fileUrl = accountReportService.createAccountsReportByFilter(request);
        String message = messageSource.getMessage("report.accounts.success", null, LocaleContextHolder.getLocale());
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(201, message, fileUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/test-pdf")
    public ResponseEntity<ApiResponseWrapper<String>> getMockAccountReport(@Valid @RequestBody PersonalAccountRequest request) throws Exception {
        String fileUrl = accountReportService.createMockAccountReport(request);
        String message = messageSource.getMessage("report.mock.success", null, LocaleContextHolder.getLocale());
        ApiResponseWrapper<String> response = new ApiResponseWrapper<>(201, message, fileUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
