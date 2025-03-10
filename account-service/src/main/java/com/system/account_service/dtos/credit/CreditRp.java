package com.system.account_service.dtos.credit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditRp implements Serializable {
    private String id;
    private String bankingAccount;
    private String accountCommon;
    private String interestRate;
    private String creditLimit;
    private String branch;
    private String debtBalance;
    private String billingCycle;
    private String lastPaymentDate;
    private String createAt;
}
