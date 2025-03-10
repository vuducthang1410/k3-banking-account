package org.demo.loanservice.dto.projection;

import java.math.BigDecimal;

public interface LoanProductReportProjection {
    String getId();
    String getNameProduct();
    BigDecimal getTotalAmountLoanDisbursement();
    BigDecimal getTotalAmountLoanIsRepayment();
    BigDecimal getTotalAmountInterestIsPayment();
    BigDecimal getTotalAmountInterest();
}
