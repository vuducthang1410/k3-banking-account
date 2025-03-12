package com.example.reporting_service.controller;

import com.example.reporting_service.model.dto.ApiResponseWrapper;
import com.example.reporting_service.model.entity.Report;
import com.example.reporting_service.service.ReportService;
import com.system.common_library.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reports") // Định nghĩa endpoint chung
@Validated
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<Report>>> getReports(
            @RequestParam @NotNull String createdBy,
            @RequestParam @NotNull ReportType reportType,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {

        Page<Report> reports = reportService.getReports(createdBy, reportType, startDate, endDate, pageable);
        return ResponseEntity.ok(new ApiResponseWrapper<>(200, "Success", reports));
    }
}
