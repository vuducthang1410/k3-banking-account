package org.demo.loanservice.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LoanDetailActiveHistoryProjection {
    String getId();
    LocalDateTime getDueDate();
    LocalDateTime getLoanDate();
    BigDecimal getAmountDisbursement();
    String getNameProduct();
    Integer getLoanTerm();
    String getNameTerm();
    LocalDateTime getDueDateRepaymentTerm();
    BigDecimal getAmountInterest();
    BigDecimal getAmountRepayment();
    BigDecimal getAmountDeftPaid();
}
