package com.example.reporting_service.repository;

import com.example.reporting_service.model.entity.Report;
import com.system.common_library.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {

    Page<Report> findByCreatedByAndReportTypeAndCreatedAtBetween(
            String createdBy,
            ReportType reportType,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}
