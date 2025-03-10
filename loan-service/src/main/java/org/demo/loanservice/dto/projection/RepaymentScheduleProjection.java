package org.demo.loanservice.dto.projection;

import java.math.BigDecimal;
import java.sql.Timestamp;

public interface RepaymentScheduleProjection {

    String getId();

    String getName();

    BigDecimal getAmountInterest();

    BigDecimal getAmountRepayment();

    Timestamp getDueDate();

    Boolean getIsPaid();

    Boolean getIsPaidInterest();

    Timestamp getPaymentInterestRate();

    Timestamp getPaymentScheduleDate();

    String getStatus();

    BigDecimal getAmountFinedRemaining();

    BigDecimal getTotalFinedAmount();
}
