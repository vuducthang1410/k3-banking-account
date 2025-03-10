package org.demo.loanservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransactionInfoDto {
    private BigDecimal balanceRemaining;
    private BigDecimal totalPayment;

    public TransactionInfoDto() {
        this.balanceRemaining = BigDecimal.ZERO;
        this.totalPayment = BigDecimal.ZERO;
    }
}
