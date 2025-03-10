package org.demo.loanservice.dto.projection;

import java.time.LocalDate;

public interface LoanDetailReportProjection {
    String getLoanId();
    String getCustomerId();
    Double getLoanAmount();
    String getLoanType();
    LocalDate getLoanDate();
    String getLoanStatus();
    Double getInterestRate();

    String getUnit();

    String getLoanAccountId();
}
