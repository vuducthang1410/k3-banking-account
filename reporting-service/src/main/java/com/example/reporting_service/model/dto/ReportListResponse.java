package com.example.reporting_service.model.dto;

import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportListResponse {
    private Long reportId;
    private ReportType reportType;
    private State status;
    private String createdBy;
    private LocalDateTime createdAt;
    private String filePath; // Nếu trạng thái COMPLETED, trả về đường dẫn PDF
}