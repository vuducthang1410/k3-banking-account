package org.demo.loanservice.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LoanInfoDetailProjection {
    String getId();
    Double getInterestRate();
    String getRequestStatus();
    String getLoanStatus();
    BigDecimal getLoanAmount();
    Integer getLoanTerm();
    String getNameLoanProduct();
    LocalDateTime getCreatedDate();
}
