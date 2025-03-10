package com.system.common_library.dto.transaction.account.savings;

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
public class TransactionSavingsResultDTO implements Serializable {

    private String transactionId;
    private BigDecimal balanceSavingsAccount;
    private BigDecimal balanceBankingAccount;
}
