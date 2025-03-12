package com.system.account_service.dtos.credit;

import com.system.account_service.entities.type.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCreditByRangeDTO {
    private String branchId;
    private LocalDate startAt;
    private LocalDate endAt;
    private AccountStatus status;
}
