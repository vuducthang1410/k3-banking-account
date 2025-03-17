package org.demo.loanservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class LoanSummaryRp {
    private BigDecimal totalUnpaidRepayment;
    private BigDecimal totalPendingLoanAmount;
    private BigDecimal totalPaidRepayment;

    private BigDecimal unpaidRepaymentRatio;
    private BigDecimal pendingLoanRatio;
    private BigDecimal paidRepaymentRatio;
}