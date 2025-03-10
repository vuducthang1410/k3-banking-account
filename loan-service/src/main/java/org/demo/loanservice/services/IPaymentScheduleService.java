package org.demo.loanservice.services;

import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.dto.projection.RepaymentScheduleProjection;
import org.demo.loanservice.dto.request.DeftRepaymentRq;
import org.demo.loanservice.entities.LoanDetailInfo;
import org.demo.loanservice.entities.LoanPenalties;
import org.demo.loanservice.entities.PaymentSchedule;
import org.demo.loanservice.entities.RepaymentHistory;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

public interface IPaymentScheduleService {
    void createDeftRepaymentInfo(LoanDetailInfo loanDetailInfo);

    @Transactional
    DataResponseWrapper<Object> automaticallyRepaymentDeftPeriodically(DeftRepaymentRq deftRepaymentRq, String transactionId);

    DataResponseWrapper<Object> getListPaymentScheduleByLoanDetailInfo(String loanInfoId, Integer pageSize, Integer pageNumber, String transactionId);

    List<RepaymentScheduleProjection> getListPaymentScheduleByLoanDetailInfo(String loanInfoId, String transactionId);

    PaymentSchedule getFirstPaymentScheduleByDueDateAfterCurrentDate(String loanDetailInfo, Timestamp currentDate);

    List<PaymentSchedule> getListPaymentScheduleByDueDateAfterCurrentDate(String loanDetailInfo, Timestamp currentDate);

    DataResponseWrapper<Object> getDetailPaymentScheduleById(String id, String transactionId);

    @Transactional
    void saveAndFlushAllPaymentScheduleAndLoanPenaltiesAndRepaymentHistory(List<PaymentSchedule> paymentScheduleList,
                                                                           List<LoanPenalties> loanPenaltiesList,
                                                                           List<RepaymentHistory> repaymentHistoryList);
}
