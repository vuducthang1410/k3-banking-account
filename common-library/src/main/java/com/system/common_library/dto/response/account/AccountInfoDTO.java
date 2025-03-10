package com.system.common_library.dto.response.account;

import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInfoDTO implements Serializable {
    private String accountId;
    private String customerId;
    private String cifCode;
    private String accountNumber;
    private BigDecimal currentAccountBalance;
    private AccountType accountType;
    private ObjectStatus statusAccount;
    private String branchName;
    private BigDecimal interestRate;
    private LocalDateTime createdAt;

    //only Credit
    private BigDecimal creditLimit;
    private BigDecimal debtBalance;
    private Integer billingCycle;
}
