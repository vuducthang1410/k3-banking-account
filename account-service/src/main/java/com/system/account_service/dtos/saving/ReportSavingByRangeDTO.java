package com.system.account_service.dtos.saving;

import com.system.account_service.entities.type.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSavingByRangeDTO {
    private String branchId;
    private BigDecimal startBalance;
    private BigDecimal endBalance;
    private LocalDate startAt;
    private LocalDate endAt;
    private AccountStatus status;
}
