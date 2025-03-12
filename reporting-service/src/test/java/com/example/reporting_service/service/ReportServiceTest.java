package com.example.reporting_service.service;

import com.example.reporting_service.model.entity.Report;
import com.example.reporting_service.repository.ReportRepository;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private Report report;

    @BeforeEach
    void setUp() {
        report = Report.builder()
                .id(1L)
                .reportType(ReportType.ACCOUNT)
                .status(State.PENDING)
                .createdBy("test_user")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createReport_ShouldReturnSavedReport() {
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        Report createdReport = reportService.createReport(ReportType.ACCOUNT, "test_user");

        assertThat(createdReport).isNotNull();
        assertThat(createdReport.getReportType()).isEqualTo(ReportType.ACCOUNT);
        assertThat(createdReport.getStatus()).isEqualTo(State.PENDING);
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void updateReportStatus_ShouldUpdateStatusAndUrl() {
        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        reportService.updateReportStatus(1L, State.COMPLETED, "test_url.pdf");

        assertThat(report.getStatus()).isEqualTo(State.COMPLETED);
        assertThat(report.getFilePath()).isEqualTo("test_url.pdf");
        verify(reportRepository, times(1)).save(report);
    }

    @Test
    void getReports_ShouldReturnPaginatedReports() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        List<Report> reportList = IntStream.range(0, 10)
                .mapToObj(i -> Report.builder()
                        .id((long) i)
                        .reportType(ReportType.ACCOUNT)
                        .status(State.PENDING)
                        .createdBy("test_user")
                        .createdAt(LocalDateTime.now().minusDays(i))
                        .build())
                .collect(Collectors.toList());
        Page<Report> reportPage = new PageImpl<>(reportList, pageable, reportList.size());

        when(reportRepository.findByCreatedByAndReportTypeAndCreatedAtBetween(
                anyString(), any(ReportType.class), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(reportPage);

        Page<Report> result = reportService.getReports("test_user", ReportType.ACCOUNT,
                LocalDateTime.now().minusDays(30), LocalDateTime.now(), pageable);

        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getContent()).hasSize(10);
        verify(reportRepository, times(1)).findByCreatedByAndReportTypeAndCreatedAtBetween(anyString(), any(ReportType.class), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class));
    }
}
