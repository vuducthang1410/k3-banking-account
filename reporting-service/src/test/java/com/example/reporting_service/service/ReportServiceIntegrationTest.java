package com.example.reporting_service.service;


import com.example.reporting_service.model.entity.Report;
import com.example.reporting_service.repository.ReportRepository;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ReportServiceIntegrationTest {
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportService reportService;

    private final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 3, 1, 12, 0);

//    @BeforeEach
//    void setUp() {
//        reportRepository.deleteAll(); // Xóa toàn bộ dữ liệu cũ
//
//        IntStream.range(0, 10).forEach(i -> {
//            Report report = Report.builder()
//                    .reportType(ReportType.ACCOUNT)
//                    .status(State.PENDING)
//                    .createdBy("test_user") // Đảm bảo đúng giá trị
//                    .createdAt(FIXED_TIME.minusDays(i)) // Ngày hợp lệ
//                    .build();
//            reportRepository.save(report);
//        });
//
//        System.out.println("Total reports after setup: " + reportRepository.count());
//    }


    @Test
    void createReport_ShouldSaveAndReturnReport() {

        // Act: Tạo report mới
        Report newReport = reportService.createReport(ReportType.LOAN, "admin_user");

        // Assert: Kiểm tra report đã được lưu thành công
        assertThat(newReport).isNotNull();
        assertThat(newReport.getId()).isNotNull();
        assertThat(newReport.getReportType()).isEqualTo(ReportType.LOAN);
        assertThat(newReport.getStatus()).isEqualTo(State.PENDING);
        assertThat(newReport.getCreatedBy()).isEqualTo("admin_user");

        // Kiểm tra số lượng report đã tăng lên 11
        List<Report> reports = reportRepository.findAll();
        assertThat(reports).hasSize(11);
    }

    @Test
    void updateReportStatus_ShouldUpdateStatusAndUrl() {
        // Arrange: Lấy report đầu tiên từ database
        Report existingReport = reportRepository.findAll().get(0);
        Long reportId = existingReport.getId();

        // Act: Cập nhật trạng thái report
        reportService.updateReportStatus(reportId, State.COMPLETED, "test_url.pdf");

        // Assert: Kiểm tra trạng thái đã được cập nhật
        Optional<Report> updatedReportOpt = reportRepository.findById(reportId);
        assertThat(updatedReportOpt).isPresent();

        Report updatedReport = updatedReportOpt.get();
        assertThat(updatedReport.getStatus()).isEqualTo(State.COMPLETED);
        assertThat(updatedReport.getFilePath()).isEqualTo("test_url.pdf");
    }

    @Test
    void getReports_ShouldReturnPaginatedReports() {
        // Arrange: Tạo Pageable để lấy dữ liệu phân trang
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        // Act: Lấy danh sách report theo tiêu chí
        Page<Report> result = reportService.getReports(
                "test_user",
                ReportType.ACCOUNT,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now(), pageable
        );

        // Assert: Kiểm tra danh sách report không rỗng và có dữ liệu hợp lệ
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty(); // Chỉ kiểm tra có dữ liệu, không quan tâm số lượng chính xác
    }
}

