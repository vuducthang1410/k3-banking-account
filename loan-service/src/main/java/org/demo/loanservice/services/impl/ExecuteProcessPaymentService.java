package org.demo.loanservice.services.impl;

import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanPaymentTransactionDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanTransactionDTO;
import com.system.common_library.dto.transaction.loan.TransactionLoanResultDTO;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.TransactionDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.common.MessageData;
import org.demo.loanservice.common.MessageValue;
import org.demo.loanservice.common.Util;
import org.demo.loanservice.controllers.exception.DataNotValidException;
import org.demo.loanservice.dto.TransactionInfoDto;
import org.demo.loanservice.dto.enumDto.DeftRepaymentStatus;
import org.demo.loanservice.dto.enumDto.PaymentTransactionType;
import org.demo.loanservice.dto.enumDto.PaymentType;
import org.demo.loanservice.entities.LoanPenalties;
import org.demo.loanservice.entities.PaymentSchedule;
import org.demo.loanservice.entities.RepaymentHistory;
import org.demo.loanservice.repositories.LoanPenaltiesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExecuteProcessPaymentService {
    private final Logger log = LogManager.getLogger(ExecuteProcessPaymentService.class);
    private final LoanPenaltiesRepository loanPenaltiesRepository;
    private final Util util;
    @DubboReference
    private AccountDubboService accountDubboService;
    @DubboReference
    private TransactionDubboService transactionDubboService;
    public TransactionLoanResultDTO paymentLoan(PaymentSchedule paymentSchedule,
                                                 AccountInfoDTO accountBankingDTO,
                                                 AccountInfoDTO accountLoanInfoDTO,
                                                 TransactionInfoDto transactionInfoDto,
                                                 List<RepaymentHistory> repaymentHistoryList) {
        TransactionLoanResultDTO transactionLoanResultDTO = processPayment(paymentSchedule, accountBankingDTO, accountLoanInfoDTO, paymentSchedule.getAmountRepayment(), PaymentType.INTEREST.name());
        transactionInfoDto.setBalanceRemaining(transactionLoanResultDTO.getBalanceBankingAccount());
        transactionInfoDto.setTotalPayment(transactionInfoDto.getTotalPayment().add(paymentSchedule.getAmountRepayment()));
        log.debug("Banking balance after payment loan:{}", transactionInfoDto.getBalanceRemaining().stripTrailingZeros().toPlainString());
        log.debug("Total amount transaction after payment loan:{}", transactionInfoDto.getTotalPayment().toPlainString());
        paymentSchedule.setPaymentScheduleDate(Timestamp.valueOf(LocalDateTime.now()));
        paymentSchedule.setIsPaid(true);
        createRepaymentHistory(repaymentHistoryList, transactionLoanResultDTO, paymentSchedule.getAmountRepayment(), PaymentType.PRINCIPAL.name(), PaymentTransactionType.PRINCIPAL_PAYMENT, paymentSchedule);
        return transactionLoanResultDTO;
    }

    public TransactionLoanResultDTO paymentInterest(PaymentSchedule paymentSchedule,
                                                     AccountInfoDTO accountBankingDTO,
                                                     AccountInfoDTO accountLoanInfoDTO,
                                                     TransactionInfoDto transactionInfoDto,
                                                     List<RepaymentHistory> repaymentHistoryList) {

        CreateLoanTransactionDTO loanInterestPaymentTransactionDTO = CreateLoanTransactionDTO.builder()
                .loanAccount(accountLoanInfoDTO.getAccountNumber())
                .note(DeftRepaymentStatus.EARLY_PAID.name())
                .cifCode(paymentSchedule.getLoanDetailInfo().getFinancialInfo().getCifCode())
                .description(DeftRepaymentStatus.EARLY_PAID.name())
                .amount(paymentSchedule.getAmountInterestRate())
                .build();
        transactionDubboService.createLoanTransaction(loanInterestPaymentTransactionDTO);
        TransactionLoanResultDTO transactionLoanResultDTO = processPayment(paymentSchedule, accountBankingDTO, accountLoanInfoDTO, paymentSchedule.getAmountInterestRate(), PaymentType.PRINCIPAL.name());
        transactionInfoDto.setBalanceRemaining(transactionLoanResultDTO.getBalanceBankingAccount());
        transactionInfoDto.setTotalPayment(transactionInfoDto.getTotalPayment().add(paymentSchedule.getAmountInterestRate()));
        log.debug("Banking balance after payment interest:{}", transactionInfoDto.getBalanceRemaining().stripTrailingZeros().toPlainString());
        log.debug("Total amount transaction after payment interest:{}", transactionInfoDto.getTotalPayment().toPlainString());
        //update info payment schedule
        paymentSchedule.setIsPaidInterest(true);
        paymentSchedule.setPaymentInterestDate(Timestamp.valueOf(LocalDateTime.now()));
        createRepaymentHistory(repaymentHistoryList, transactionLoanResultDTO, paymentSchedule.getAmountInterestRate(), "", PaymentTransactionType.INTEREST_PAYMENT, paymentSchedule);
        return transactionLoanResultDTO;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionLoanResultDTO paymentPenalty(PaymentSchedule paymentSchedule,
                                                      AccountInfoDTO accountBankingDTO,
                                                      AccountInfoDTO accountLoanInfoDTO,
                                                      TransactionInfoDto transactionInfoDto,
                                                      List<RepaymentHistory> repaymentHistoryList) {
        Set<LoanPenalties> loanPenaltiesSet = paymentSchedule.getLoanPenaltiesSet();

        // Calculate the total penalty amount
        BigDecimal totalFineAmount = loanPenaltiesSet.stream()
                .filter(lp -> !lp.getIsPaid())
                .map(LoanPenalties::getFinedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TransactionLoanResultDTO transactionLoanResultDTO = processPayment(paymentSchedule, accountBankingDTO, accountLoanInfoDTO, totalFineAmount, PaymentType.PENALTY.name());
        transactionInfoDto.setTotalPayment(transactionInfoDto.getTotalPayment().add(totalFineAmount));
        log.debug("Banking balance after payment penalty:{}", transactionInfoDto.getBalanceRemaining().stripTrailingZeros().toPlainString());
        log.debug("Total amount transaction after payment penalty: {}", transactionInfoDto.getTotalPayment().toPlainString());
        // Mark penalties as paid and update the payment date
        for (LoanPenalties penalty : loanPenaltiesSet) {
            penalty.setIsPaid(true);
            penalty.setFinedPaymentDate(new Date(System.currentTimeMillis()));
        }
        // Save updated penalty records
        loanPenaltiesRepository.saveAll(loanPenaltiesSet);
        paymentSchedule.setLoanPenaltiesSet(loanPenaltiesSet);
        createRepaymentHistory(repaymentHistoryList, transactionLoanResultDTO, totalFineAmount, "", PaymentTransactionType.OVERDUE_PAYMENT_PENALTY, paymentSchedule);
        return transactionLoanResultDTO;
    }
    private TransactionLoanResultDTO processPayment(PaymentSchedule paymentSchedule,
                                                    AccountInfoDTO accountBankingDTO,
                                                    AccountInfoDTO accountLoanInfoDTO,
                                                    BigDecimal amount,
                                                    String paymentType) {
        // Ensure sufficient balance to cover the principal amount
        if (accountBankingDTO.getCurrentAccountBalance().compareTo(amount) < 0) {
            log.info("Insufficient funds to repay the principal amount.");
            throw new DataNotValidException(MessageData.ACCOUNT_BALANCE_NOT_ENOUGH);
        }
        String typeTransaction = paymentType.equals(PaymentType.PRINCIPAL.name()) ? MessageValue.CONTENT_TRANSACTION_PRINCIPAL :
                (paymentType.equals(PaymentType.INTEREST.name()) ? MessageValue.CONTENT_TRANSACTION_INTEREST : MessageValue.CONTENT_TRANSACTION_PENALTY);
        String description = util.getMessageTransactionFromMessageSource(
                typeTransaction,
                paymentSchedule.getLoanDetailInfo().getLoanProductId().getNameProduct(),
                paymentSchedule.getName(),
                amount.stripTrailingZeros().toPlainString());

        // Call transaction service to process the payment
        CreateLoanPaymentTransactionDTO paymentLoanDTO = CreateLoanPaymentTransactionDTO.builder()
                .note(paymentType)
                .amount(amount)
                .cifCode(paymentSchedule.getLoanDetailInfo().getFinancialInfo().getCifCode())
                .isCashPayment(false)
                .description(description)
                .paymentAccount(accountBankingDTO.getAccountNumber())
                .loanAccount(accountLoanInfoDTO.getAccountNumber())
                .build();
        return transactionDubboService.createLoanPaymentTransaction(paymentLoanDTO);
    }
    private void createRepaymentHistory(List<RepaymentHistory> repaymentHistoryList,
                                        TransactionLoanResultDTO transactionLoanResultDTO,
                                        BigDecimal amountPayment,
                                        String note,
                                        PaymentTransactionType paymentType,
                                        PaymentSchedule paymentSchedule
    ) {
        RepaymentHistory repaymentHistory = new RepaymentHistory();
        repaymentHistory.setTransactionId(transactionLoanResultDTO.getTransactionId());
        repaymentHistory.setPaymentSchedule(paymentSchedule);
        repaymentHistory.setAmountPayment(amountPayment);
        repaymentHistory.setPaymentType(paymentType);
        repaymentHistory.setNote(note);
        repaymentHistoryList.add(repaymentHistory);
    }

}
