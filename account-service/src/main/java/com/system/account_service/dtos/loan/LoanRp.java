package com.system.account_service.dtos.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRp implements Serializable {
    private String id;
    private String accountCommon;
    private String bankingAccount;
    private String balance;
    private String branch;
    private String createAt;
}
