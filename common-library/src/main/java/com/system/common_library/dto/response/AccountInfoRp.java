package com.system.common_library.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccountInfoRp implements Serializable {
    private String bankingAccountNumber;
    private String loanAccountNumber;
    private String statusBankingAccount;
    private String statusLoanAccount;
}
