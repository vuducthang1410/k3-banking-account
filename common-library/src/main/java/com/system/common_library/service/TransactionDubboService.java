package com.system.common_library.service;

import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.dto.report.TransactionReportRequest;
import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.transaction.TransactionDTO;
import com.system.common_library.dto.transaction.TransactionExtraDTO;
import com.system.common_library.dto.transaction.account.credit.CreateCreditDisbursementTransactionDTO;
import com.system.common_library.dto.transaction.account.credit.CreateCreditPaymentTransactionDTO;
import com.system.common_library.dto.transaction.account.credit.CreateCreditTransactionDTO;
import com.system.common_library.dto.transaction.account.credit.TransactionCreditResultDTO;
import com.system.common_library.dto.transaction.account.savings.CreateSavingsDisbursementTransactionDTO;
import com.system.common_library.dto.transaction.account.savings.CreateSavingsPaymentTransactionDTO;
import com.system.common_library.dto.transaction.account.savings.CreateSavingsTransactionDTO;
import com.system.common_library.dto.transaction.account.savings.TransactionSavingsResultDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanDisbursementTransactionDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanPaymentTransactionDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanTransactionDTO;
import com.system.common_library.dto.transaction.loan.TransactionLoanResultDTO;
import com.system.common_library.exception.DubboException;

import java.util.List;

public interface TransactionDubboService {

    // Get transaction detail (Lấy thông tin chi tiết giao dịch)
    TransactionExtraDTO getTransactionDetail(String id) throws DubboException;

    // Get transaction list by account number (Lấy lịch sử giao dịch theo số tài khoản)
    PagedDTO<TransactionDTO> getTransactionListByAccount(String account, int page, int limit) throws DubboException;

    // Get transaction list by CIF code (Lấy lịch sử giao dịch theo mã CIF)
    PagedDTO<TransactionDTO> getTransactionListByCIF(String cif, int page, int limit) throws DubboException;

    // Loan account disbursement (Giải ngân cho tài khoản vay)
    TransactionLoanResultDTO createLoanAccountDisbursement(CreateLoanDisbursementTransactionDTO create) throws DubboException;
    // Rollback cho loan account disbursement (Giải ngân cho tài khoản vay)
    boolean rollbackLoanAccountDisbursement(String transactionId) throws DubboException;

    // Interest/penalty transaction for loan account (Tính lãi suất/tiền phạt cho tài khoản vay)
    TransactionLoanResultDTO createLoanTransaction(CreateLoanTransactionDTO create) throws DubboException;
    // Rollback cho interest/penalty transaction for loan account (Tính lãi suất/tiền phạt cho tài khoản vay)
    boolean rollbackLoanTransaction(String transactionId) throws DubboException;

    // Loan account payment (Thanh toán cho tài khoản vay)
    TransactionLoanResultDTO createLoanPaymentTransaction(CreateLoanPaymentTransactionDTO create) throws DubboException;
    // Rollback cho loan account payment (Thanh toán cho tài khoản vay)
    boolean rollbackLoanPaymentTransaction(String transactionId) throws DubboException;

    // Savings account payment (Gửi tiền vào tài khoản tiết kiệm)
    TransactionSavingsResultDTO createSavingsPaymentTransaction(CreateSavingsDisbursementTransactionDTO create) throws DubboException;
    // Rollback cho savings account payment (Gửi tiền vào tài khoản tiết kiệm)
    boolean rollbackSavingsPaymentTransaction(String transactionId) throws DubboException;

    // Interest transaction for savings account (Tính lãi suất cho tài khoản tiết kiệm)
    TransactionSavingsResultDTO createSavingsTransaction(CreateSavingsTransactionDTO create) throws DubboException;
    // Rollback cho interest transaction for savings account (Tính lãi suất cho tài khoản tiết kiệm)
    boolean rollbackSavingsTransaction(String transactionId) throws DubboException;

    // Closing savings account (Tất toán tài khoản tiết kiệm)
    TransactionSavingsResultDTO createSavingsClosingTransaction(CreateSavingsPaymentTransactionDTO create) throws DubboException;
    // Rollback cho closing savings account (Tất toán tài khoản tiết kiệm)
    boolean rollbackSavingsClosingTransaction(String transactionId) throws DubboException;

    // Credit account disbursement (Giải ngân cho tài khoản tín dụng)
    TransactionCreditResultDTO createCreditAccountDisbursement(CreateCreditDisbursementTransactionDTO create) throws DubboException;
    // Rollback cho credit account disbursement (Giải ngân cho tài khoản tín dụng)
    boolean rollbackCreditAccountDisbursement(String transactionId) throws DubboException;

    // Interest transaction for credit account (Tính lãi suất cho tài khoản tín dụng)
    TransactionCreditResultDTO createCreditTransaction(CreateCreditTransactionDTO create) throws DubboException;
    // Rollback cho interest transaction for credit account (Tính lãi suất cho tài khoản tín dụng)
    boolean rollbackCreditTransaction(String transactionId) throws DubboException;

    // Credit account payment (Thanh toán cho tài khoản tín dụng)
    TransactionCreditResultDTO createCreditPaymentTransaction(CreateCreditPaymentTransactionDTO create) throws DubboException;
    // Rollback cho credit account payment (Thanh toán cho tài khoản tín dụng)
    boolean rollbackCreditPaymentTransaction(String transactionId) throws DubboException;

    // gRPC for report
    // Get transaction by field filter(Lấy danh sách các giai dịch theo điều kiện)
    List<TransactionReportDTO> getTransactionByFilter(TransactionReportRequest request) throws DubboException;
}
