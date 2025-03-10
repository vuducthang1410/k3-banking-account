package com.example.reporting_service.service;

import com.example.reporting_service.model.entity.Report;
import com.example.reporting_service.repository.ReportRepository;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportStatusService {

    @Autowired
    private ReportRepository reportRepository;

    public Report createReport(ReportType reportType) {
        Report report = new Report();
        report.setReportType(reportType);
        report.setStatus(State.PENDING);
        return reportRepository.save(report);
    }

    public void updateReportStatus(Long reportId, State status) {
        reportRepository.findById(reportId).ifPresent(report -> {
            report.setStatus(status);
            reportRepository.save(report);
        });
    }
}
