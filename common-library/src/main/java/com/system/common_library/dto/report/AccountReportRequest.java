package com.system.common_library.dto.report;

import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountReportRequest implements Serializable {
    private AccountType accountType;
    private String bankBranch;
    private Double startBalance;
    private Double endBalance;
    private LocalDate startAt;
    private LocalDate endAt;
    private ObjectStatus status;
}
