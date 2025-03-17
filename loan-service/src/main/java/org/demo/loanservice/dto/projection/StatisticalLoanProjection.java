package org.demo.loanservice.dto.projection;

import java.math.BigDecimal;

public interface StatisticalLoanProjection {
    BigDecimal getTotalUnpaidRepayment();
    BigDecimal getTotalPendingLoanAmount();
    BigDecimal getTotalPaidRepayment();
}
