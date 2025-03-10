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

    @Column(name = "report_code", unique = true, nullable = false)
    private String reportCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private State status;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
            this.createdAt = LocalDateTime.now();
    }
}
