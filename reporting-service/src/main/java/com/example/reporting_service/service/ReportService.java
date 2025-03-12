package com.example.reporting_service.service;

import com.example.reporting_service.model.entity.Report;
import com.example.reporting_service.repository.ReportRepository;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    public Report createReport(ReportType reportType, String createdBy) {
        Report report = Report.builder()
                .reportType(reportType)
                .status(State.PENDING)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();
        return reportRepository.save(report);
    }

    public void updateReportStatus(Long reportId, State status, String url) {
        reportRepository.findById(reportId).ifPresent(report -> {
            report.setStatus(status);
            report.setFilePath(url);
            reportRepository.save(report);
        });
    }

    public Page<Report> getReports(String createdBy, ReportType reportType, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return reportRepository.findByCreatedByAndReportTypeAndCreatedAtBetween(createdBy,reportType, startDate, endDate, pageable);
    }
}
