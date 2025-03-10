package org.demo.loanservice.dto.projection;

import java.math.BigDecimal;

public interface LoanAmountInfoProjection {
    BigDecimal getTotalLoanedAmount();
    BigDecimal getLoanAmountMax();
}
