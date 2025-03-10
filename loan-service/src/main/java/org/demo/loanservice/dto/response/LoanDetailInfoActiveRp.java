package org.demo.loanservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoanDetailInfoActiveRp {
    private String loanInfoId;
    private String dueDate;
    private String nextRepaymentDate;
    private String loanDate;
    private String loanAmount;
    private String loanAmountRemaining;
    private String nextLoanAmountRepayment;
    private String loanProductName;
    private String loanTermName;
    private Integer loanTerm;
}
