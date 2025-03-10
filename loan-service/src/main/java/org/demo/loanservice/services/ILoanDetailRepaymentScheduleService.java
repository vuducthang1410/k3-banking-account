package org.demo.loanservice.services;

import org.demo.loanservice.entities.LoanDetailInfo;

public interface ILoanDetailRepaymentScheduleService {
    LoanDetailInfo getLoanDetailInfoById(String loanDetailInfoId, String transactionId);
    void updateLoanStatus(String loanDetailInfoId, String transactionId);
}
