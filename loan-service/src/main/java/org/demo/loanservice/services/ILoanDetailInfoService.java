package org.demo.loanservice.services;

import org.demo.loanservice.common.DataResponseWrapper;
import org.demo.loanservice.dto.request.IndividualCustomerInfoRq;
import org.demo.loanservice.dto.request.LoanInfoApprovalRq;

public interface ILoanDetailInfoService{
    DataResponseWrapper<Object> registerIndividualCustomerLoan(IndividualCustomerInfoRq individualCustomerInfoRq, String transactionId);
    DataResponseWrapper<Object> approveIndividualCustomerDisbursement(LoanInfoApprovalRq loanInfoApprovalRq, String transactionId);

    DataResponseWrapper<Object> getAllByLoanStatus(String loanStatus, Integer pageNumber, Integer pageSize, String transactionId);

    DataResponseWrapper<Object> cancelLoanRequest(String id, String transactionId);

    //todo:
    DataResponseWrapper<Object> earlyPaymentLoan(String transactionId, String loanInfoId);

    //todo
    DataResponseWrapper<Object> getEarlyPaymentPenaltyFee(String loanInfoId, String transactionId);

    DataResponseWrapper<Object> getAllByCifCode(Integer pageNumber, Integer pageSize, String transactionId,String requestStatus, String cifCode);

    DataResponseWrapper<Object> getAllLoanIsActive(Integer pageNumber, Integer pageSize, String transactionId, String cifCode);

    DataResponseWrapper<Object> getDetailByLoanInfoDetailId(String loanInfoId, String transactionId);

    DataResponseWrapper<Object> getLoanReportByCifCode(String cifCode, String transactionId);
}
