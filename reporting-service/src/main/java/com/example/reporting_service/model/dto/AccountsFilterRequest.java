package com.example.reporting_service.model.dto;

import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsFilterRequest {
    private AccountType accountType;
    private String bankBranch;
    private Double startBalance;
    private Double endBalance;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endAt;

    private ObjectStatus status;

}
