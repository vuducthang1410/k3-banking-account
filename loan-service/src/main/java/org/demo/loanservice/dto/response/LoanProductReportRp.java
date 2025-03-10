package org.demo.loanservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanProductReportRp {
    private String loanProductId;
    private String loanProductName;
    private BigDecimal totalAmountLoanNotRepayment;
    private BigDecimal totalAmountLoanIsRepayment;
    private BigDecimal totalAmountInterestIsPayment;
    private BigDecimal totalAmountInterestNotPayment;
}
