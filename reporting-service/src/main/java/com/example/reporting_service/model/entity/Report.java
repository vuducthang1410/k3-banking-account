package com.example.reporting_service.model.entity;

import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private State status = State.PENDING; // Giá trị mặc định

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", updatable = false, nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // Giá trị mặc định

    @Column(name = "file_path", length = 500)
    private String filePath; // Lưu đường dẫn file thay vì dữ liệu PDF

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}