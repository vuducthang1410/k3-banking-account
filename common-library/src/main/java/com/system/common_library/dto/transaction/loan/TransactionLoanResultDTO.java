package com.system.common_library.dto.transaction.loan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLoanResultDTO implements Serializable {

    private String transactionId;
    private BigDecimal balanceLoanAccount;
    private BigDecimal balanceBankingAccount;
}
