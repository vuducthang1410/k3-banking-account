package com.example.reporting_service.model.dto;

import com.system.common_library.enums.AccountType;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalLoanRequest {
    private String loanId;
    private String account;
    private AccountType accountType;
    private String customerId;
}
