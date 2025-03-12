package org.demo.loanservice.services.impl;


import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.transaction.loan.TransactionLoanResultDTO;
import com.system.common_library.enums.ObjectStatus;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.TransactionDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.common.*;
import org.demo.loanservice.controllers.exception.DataNotFoundException;
import org.demo.loanservice.controllers.exception.DataNotValidException;
import org.demo.loanservice.controllers.exception.ServerErrorException;
import org.demo.loanservice.dto.MapEntityToDto;
import org.demo.loanservice.dto.TransactionInfoDto;
import org.demo.loanservice.dto.enumDto.DeftRepaymentStatus;
import org.demo.loanservice.dto.enumDto.FormDeftRepaymentEnum;
import org.demo.loanservice.dto.enumDto.PaymentType;
import org.demo.loanservice.dto.projection.RepaymentScheduleProjection;
import org.demo.loanservice.dto.request.DeftRepaymentRq;
import org.demo.loanservice.dto.response.PaymentScheduleDetailRp;
import org.demo.loanservice.dto.response.PaymentScheduleRp;
import org.demo.loanservice.entities.LoanDetailInfo;
import org.demo.loanservice.entities.LoanPenalties;
import org.demo.loanservice.entities.PaymentSchedule;
import org.demo.loanservice.entities.RepaymentHistory;
import org.demo.loanservice.repositories.LoanPenaltiesRepository;
import org.demo.loanservice.repositories.PaymentScheduleRepository;
import org.demo.loanservice.repositories.RepaymentHistoryRepository;
import org.demo.loanservice.services.ILoanDetailRepaymentScheduleService;
import org.demo.loanservice.services.IPaymentScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentScheduleServiceImpl implements IPaymentScheduleService {
    private final PaymentScheduleRepository paymentScheduleRepository;
    private final LoanPenaltiesRepository loanPenaltiesRepository;
    private final ILoanDetailRepaymentScheduleService loanDetailRepaymentScheduleService;
    private final RepaymentHistoryRepository repaymentHistoryRepository;
    private final ExecuteProcessPaymentService executeProcessPaymentService;
    private final Util util;
    @DubboReference
    private AccountDubboService accountDubboService;
    @DubboReference
    private TransactionDubboService transactionDubboService;
    private final Logger log = LogManager.getLogger(PaymentScheduleServiceImpl.class);

    @Override
    public void createDeftRepaymentInfo(LoanDetailInfo loanDetailInfo) {
        List<PaymentSchedule> paymentScheduleList = new LinkedList<>();
        log.debug(" form deft repayment : {}", loanDetailInfo.getFormDeftRepayment().name());

        if (loanDetailInfo.getLoanTerm() <= 0) {
            log.warn("Invalid loan term {} for loan account {}. Skipping repayment schedule creation.", loanDetailInfo.getLoanTerm(), loanDetailInfo.getDisbursementInfoHistory().getLoanAccountId());
            return;
        }
        /*
            loan amount repayment every term = loan amount / loan term
         */
        BigDecimal amountRepaymentEveryTerm = loanDetailInfo.getLoanAmount()
                .divide(new BigDecimal(loanDetailInfo.getLoanTerm()), RoundingMode.HALF_UP);
        log.debug("amount repayment every term : {}", amountRepaymentEveryTerm.stripTrailingZeros().toPlainString());
        if (loanDetailInfo.getFormDeftRepayment().equals(FormDeftRepaymentEnum.PRINCIPAL_AND_INTEREST_MONTHLY)) {
            /*
                amount interest = loan amount * interest rate/100
             */
            BigDecimal amountInterest = loanDetailInfo.getLoanAmount().multiply(BigDecimal.valueOf(loanDetailInfo.getInterestRate()))
                    .divide(new BigDecimal(100), RoundingMode.HALF_UP);
            log.debug("Calculated interest amount for loan account {}: {}", loanDetailInfo.getDisbursementInfoHistory().getLoanAccountId(), amountInterest.stripTrailingZeros().toPlainString());

            for (int i = 0; i < loanDetailInfo.getLoanTerm(); i++) {
                PaymentSchedule paymentSchedule = createPaymentSchedule(loanDetailInfo, amountRepaymentEveryTerm, amountInterest, i);
                paymentScheduleList.add(paymentSchedule);
            }
        } else if (loanDetailInfo.getFormDeftRepayment().equals(FormDeftRepaymentEnum.PRINCIPAL_INTEREST_DECREASING)) {
            for (int i = 0; i < loanDetailInfo.getLoanTerm(); i++) {
               /*
                   amount interest = remain loan amount * interest rate/100
                */
                BigDecimal remainingAmount = amountRepaymentEveryTerm.multiply(new BigDecimal(loanDetailInfo.getLoanTerm() - i));
                BigDecimal amountInterest = remainingAmount
                        .multiply(BigDecimal.valueOf(loanDetailInfo.getInterestRate()))
                        .divide(new BigDecimal(100), RoundingMode.HALF_UP);
                log.debug("Term :{} - remain amount : {} - interest amount: {}", i, remainingAmount.toPlainString(), amountInterest.toPlainString());

                PaymentSchedule paymentSchedule = createPaymentSchedule(loanDetailInfo, amountRepaymentEveryTerm, amountInterest, i);
                paymentScheduleList.add(paymentSchedule);
            }
        }
        paymentScheduleRepository.saveAllAndFlush(paymentScheduleList);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DataResponseWrapper<Object> automaticallyRepaymentDeftPeriodically(DeftRepaymentRq deftRepaymentRq, String transactionId) {
        // Retrieve the payment schedule and ensure it exists
        PaymentSchedule paymentSchedule = paymentScheduleRepository.findByIdAndIsDeleted(deftRepaymentRq.getPaymentScheduleId(), false)
                .orElseThrow(() -> {
                    log.warn("Transaction {}: Payment schedule with ID {} not found.", transactionId, deftRepaymentRq.getPaymentScheduleId());
                    return new DataNotFoundException(MessageData.PAYMENT_SCHEDULE_NOT_FOUND);
                });

        // Validate if the payment has already been completed
        if (paymentSchedule.getIsPaid() && paymentSchedule.getIsPaidInterest()) {
            log.warn("Transaction {}: Payment already completed for schedule ID {}", transactionId, deftRepaymentRq.getPaymentScheduleId());
            throw new DataNotValidException(MessageData.PAYMENT_SCHEDULE_COMPLETED);
        }
        // Initialize repayment history tracking
        List<RepaymentHistory> repaymentHistoryList = new LinkedList<>();
        try {
            // Fetch customer banking and loan account details
            log.info("Transaction {}: Fetching banking account and loan account details.", transactionId);
            AccountInfoDTO accountBankingDTO = accountDubboService.getBankingAccount(paymentSchedule.getLoanDetailInfo().getFinancialInfo().getCifCode());
            AccountInfoDTO accountLoanInfoDTO = accountDubboService.getLoanAccountDTO(paymentSchedule.getLoanDetailInfo().getDisbursementInfoHistory().getLoanAccountId());

            // Ensure the banking account is active before proceeding
            if (!ObjectStatus.ACTIVE.equals(accountBankingDTO.getStatusAccount())) {
                log.warn("Transaction {}: Banking account with CIF code {} is not active (Status: {}).",
                        transactionId, paymentSchedule.getLoanDetailInfo().getFinancialInfo().getCifCode(), accountBankingDTO.getStatusAccount());
                throw new DataNotValidException(MessageData.BANKING_ACCOUNT_NOT_ACTIVE);
            }

            // Check the available balance in the banking account
            TransactionInfoDto transactionInfoDto = new TransactionInfoDto();
            transactionInfoDto.setTotalPayment(accountBankingDTO.getCurrentAccountBalance());
            log.info("Transaction {}: Current account balance is {}", transactionId,
                    transactionInfoDto.getBalanceRemaining().stripTrailingZeros().toPlainString());

            // Validate if the account has sufficient funds
            if (transactionInfoDto.getBalanceRemaining().compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Transaction {}: Insufficient balance in the banking account.", transactionId);
                throw new DataNotValidException(MessageData.ACCOUNT_BALANCE_NOT_ENOUGH);
            }

            // Process the payment based on the specified payment type (INTEREST, PRINCIPAL, PENALTY)
            log.info("Transaction {}: Initiating payment processing for schedule ID {}.", transactionId, deftRepaymentRq.getPaymentScheduleId());
            processPayment(deftRepaymentRq.getPaymentType(), paymentSchedule, accountBankingDTO, accountLoanInfoDTO, transactionInfoDto, repaymentHistoryList, transactionId);

            // Verify if the payment was processed successfully
            if (transactionInfoDto.getTotalPayment().compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Transaction {}: No payment was processed due to insufficient funds or invalid payment.", transactionId);
                throw new DataNotValidException(MessageData.ACCOUNT_BALANCE_NOT_ENOUGH);
            }


            log.info("Transaction {}: Payment successfully processed and saved.", transactionId);
            // Persist the updated payment schedule and repayment history
            paymentScheduleRepository.saveAndFlush(paymentSchedule);
            repaymentHistoryRepository.saveAllAndFlush(repaymentHistoryList);
            return DataResponseWrapper.builder()
                    .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                    .data(paymentSchedule.getId())
                    .build();
        } catch (Exception e) {
            log.error(MessageData.MESSAGE_LOG_DETAIL, transactionId, MessageData.REPAYMENT_LOAN_ERROR.getMessageLog(), e.getMessage(), e);
            throw new DataNotValidException(MessageData.REPAYMENT_LOAN_ERROR);
            //todo: rollback transaction
        }
    }

    @Override
    public DataResponseWrapper<Object> getListPaymentScheduleByLoanDetailInfo(String loanInfoId, Integer pageSize, Integer pageNumber, String transactionId) {
        log.debug("loan detail info id : {}", loanInfoId);
        LoanDetailInfo loanDetailInfo = loanDetailRepaymentScheduleService.getLoanDetailInfoById(loanInfoId, transactionId);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<RepaymentScheduleProjection> repaymentScheduleProjectionPage =
                paymentScheduleRepository.findPaymentScheduleByLoanDetailInfoId(loanDetailInfo.getId(), pageable);
        //map query result to data response
        List<PaymentScheduleRp> paymentScheduleRpList = repaymentScheduleProjectionPage
                .getContent()
                .stream()
                .map(MapEntityToDto::mapToPaymentScheduleRp)
                .toList();

        log.debug("total content is valid : {}", repaymentScheduleProjectionPage.getTotalElements());
        log.debug("Size response list : {}", paymentScheduleRpList.size());
        Map<String, Object> dataResponse = new HashMap<>();
        dataResponse.put("totalRecord", repaymentScheduleProjectionPage.getTotalElements());
        dataResponse.put("listPaymentSchedule", paymentScheduleRpList);
        return DataResponseWrapper.builder()
                .data(dataResponse)
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                .build();
    }

    @Override
    public List<RepaymentScheduleProjection> getListPaymentScheduleByLoanDetailInfo(String loanInfoId, String transactionId) {
        LoanDetailInfo loanDetailInfo = loanDetailRepaymentScheduleService.getLoanDetailInfoById(loanInfoId, transactionId);
        return paymentScheduleRepository.findPaymentScheduleByLoanDetailInfoId(loanDetailInfo.getId());
    }

    @Override
    public PaymentSchedule getFirstPaymentScheduleByDueDateAfterCurrentDate(String loanDetailInfo, Timestamp currentDate) {
        return paymentScheduleRepository.findFirstByIsDeletedFalseAndLoanDetailInfo_IdAndDueDateAfterOrderByDueDateAsc(loanDetailInfo, currentDate)
                .orElseThrow(
                        ServerErrorException::new
                );
    }

    @Override
    public List<PaymentSchedule> getListPaymentScheduleByDueDateAfterCurrentDate(String loanDetailInfo, Timestamp currentDate) {
        return paymentScheduleRepository.findAllByIsDeletedFalseAndLoanDetailInfo_IdAndDueDateAfterOrderByDueDateAsc(loanDetailInfo, currentDate);
    }

    @Override
    public DataResponseWrapper<Object> getDetailPaymentScheduleById(String id, String transactionId) {
        PaymentSchedule paymentSchedule = paymentScheduleRepository.findByIdAndIsDeleted(id, false)
                .orElseThrow(ServerErrorException::new);
        return DataResponseWrapper.builder()
                .data(mapToPaymentScheduleDetailRp(paymentSchedule))
                .status(MessageValue.STATUS_CODE_SUCCESSFULLY)
                .message(util.getMessageFromMessageSource(MessageData.FIND_SUCCESSFULLY.getKeyMessage()))
                .build();
    }

    @Transactional
    @Override
    public void saveAndFlushAllPaymentScheduleAndLoanPenaltiesAndRepaymentHistory(List<PaymentSchedule> paymentScheduleList,
                                                                                  List<LoanPenalties> loanPenaltiesList,
                                                                                  List<RepaymentHistory> repaymentHistoryList) {
        paymentScheduleRepository.saveAllAndFlush(paymentScheduleList);
        loanPenaltiesRepository.saveAllAndFlush(loanPenaltiesList);
        repaymentHistoryRepository.saveAllAndFlush(repaymentHistoryList);
    }

    /**
     * Process the payment based on the type (INTEREST, PRINCIPAL, PENALTY, ALL).
     */
    private void processPayment(String paymentType, PaymentSchedule paymentSchedule, AccountInfoDTO accountBankingDTO,
                                AccountInfoDTO accountLoanInfoDTO, TransactionInfoDto transactionInfoDto, List<RepaymentHistory> repaymentHistoryList, String transactionId) {
        // Process interest payment
        if (shouldProcess(paymentType, PaymentType.INTEREST)&&!paymentSchedule.getIsPaidInterest()) {
            try {
                TransactionLoanResultDTO transactionLoanResultDTO = executeProcessPaymentService.paymentInterest(paymentSchedule, accountBankingDTO, accountLoanInfoDTO, transactionInfoDto, repaymentHistoryList);
                checkLoanAccountBalanceToCloseLoan(paymentSchedule,accountLoanInfoDTO, transactionLoanResultDTO, transactionId);
            } catch (Exception e) {
                log.error("Error processing interest payment: {}", e.getMessage(), e);
                return;
            }
        }

        // Process principal payment
        if (shouldProcess(paymentType, PaymentType.PRINCIPAL)&&!paymentSchedule.getIsPaid()) {
            try {
                TransactionLoanResultDTO transactionLoanResultDTO = executeProcessPaymentService.paymentLoan(paymentSchedule, accountBankingDTO, accountLoanInfoDTO, transactionInfoDto, repaymentHistoryList);
                checkLoanAccountBalanceToCloseLoan(paymentSchedule,accountLoanInfoDTO, transactionLoanResultDTO, transactionId);
            } catch (Exception e) {
                log.error("Error processing principal payment: {}", e.getMessage(), e);
                return;
            }
        }

        // Process penalty payment if overdue
        if (shouldProcess(paymentType, PaymentType.PENALTY) && isPaymentOverdue(paymentSchedule)) {
            try {
                TransactionLoanResultDTO transactionLoanResultDTO = executeProcessPaymentService.paymentPenalty(paymentSchedule, accountBankingDTO, accountLoanInfoDTO, transactionInfoDto, repaymentHistoryList);
                checkLoanAccountBalanceToCloseLoan(paymentSchedule,accountLoanInfoDTO, transactionLoanResultDTO, transactionId);
            } catch (Exception e) {
                log.error("Error processing penalty payment: {}", e.getMessage(), e);
            }
        }
    }

    private void checkLoanAccountBalanceToCloseLoan(PaymentSchedule paymentSchedule,AccountInfoDTO loaAccountInfoDto, TransactionLoanResultDTO resultExecuteTransaction, String transactionId) {
        if (resultExecuteTransaction.getBalanceLoanAccount().compareTo(BigDecimal.ZERO) == 0) {
            String loanDetailInfo = paymentSchedule.getLoanDetailInfo().getId();
            loanDetailRepaymentScheduleService.updateLoanStatus(loanDetailInfo, transactionId);
            AccountInfoDTO accountInfoDTO=accountDubboService.updateAccountStatus(loaAccountInfoDto.getAccountNumber(),ObjectStatus.CLOSED);
            if(accountInfoDTO.getStatusAccount().compareTo(ObjectStatus.CLOSED)!=0){
                throw new ServerErrorException();
            }
        }
    }

    /**
     * Check if the payment schedule is overdue by more than 3 days.
     */
    private boolean isPaymentOverdue(PaymentSchedule paymentSchedule) {
        return LocalDate.now().isAfter(paymentSchedule.getDueDate().toLocalDateTime().toLocalDate().plusDays(3));
    }

    /**
     * @param requestType: data from body request
     * @param type:        data used to compare with data from request
     */
    private boolean shouldProcess(String requestType, PaymentType type) {
        return requestType.equalsIgnoreCase(type.name()) || requestType.equalsIgnoreCase(PaymentType.ALL.name());
    }
    private PaymentSchedule createPaymentSchedule(LoanDetailInfo loanDetailInfo,
                                                  BigDecimal amountRepaymentEveryTerm,
                                                  BigDecimal amountInterestRate,
                                                  int index) {
        PaymentSchedule paymentSchedule = new PaymentSchedule();
        paymentSchedule.setLoanDetailInfo(loanDetailInfo);
        paymentSchedule.setStatus(DeftRepaymentStatus.NOT_DUE);
        paymentSchedule.setAmountRepayment(amountRepaymentEveryTerm);
        paymentSchedule.setAmountInterestRate(amountInterestRate);
        paymentSchedule.setIsPaid(false);
        paymentSchedule.setIsDeleted(false);
        paymentSchedule.setIsPaidInterest(false);
        paymentSchedule.setDueDate(DateUtil.getDateAfterNMonths(index + 1));
        paymentSchedule.setName(String.valueOf((index + 1)));
        return paymentSchedule;
    }
    private PaymentScheduleDetailRp mapToPaymentScheduleDetailRp(PaymentSchedule paymentSchedule) {
        PaymentScheduleDetailRp paymentScheduleDetailRp = new PaymentScheduleDetailRp();

        paymentScheduleDetailRp.setPaymentScheduleId(paymentSchedule.getId());
        paymentScheduleDetailRp.setNameSchedule(paymentSchedule.getName());
        paymentScheduleDetailRp.setDueDate(DateUtil.format(DateUtil.YYYY_MM_DD_HH_MM_SS, paymentSchedule.getDueDate()));

        if (paymentSchedule.getAmountInterestRate() != null) {
            paymentScheduleDetailRp.setAmountInterest(paymentSchedule.getAmountInterestRate().stripTrailingZeros().toPlainString());
        }
        if (paymentSchedule.getAmountRepayment() != null) {
            paymentScheduleDetailRp.setAmountLoan(paymentSchedule.getAmountRepayment().stripTrailingZeros().toPlainString());
        }
        //calculate penalty of payment period of loan
        BigDecimal totalPenalty = paymentSchedule.getLoanPenaltiesSet().stream()
                .map(LoanPenalties::getFinedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        paymentScheduleDetailRp.setAmountFined(totalPenalty.stripTrailingZeros().toPlainString());
        paymentScheduleDetailRp.setPaidInterest(Boolean.TRUE.equals(paymentSchedule.getIsPaidInterest()));
        paymentScheduleDetailRp.setPaidLoan(Boolean.TRUE.equals(paymentSchedule.getIsPaid()));
        paymentScheduleDetailRp.setPaidFined(!paymentSchedule.getLoanPenaltiesSet().isEmpty());
        if (paymentSchedule.getStatus() != null) {
            paymentScheduleDetailRp.setDeftRepaymentStatus(paymentSchedule.getStatus().name());
        }
        paymentScheduleDetailRp.setEnd(Boolean.TRUE.equals(paymentSchedule.getIsPaid())
                && Boolean.TRUE.equals(paymentSchedule.getIsPaidInterest()));
        if (paymentSchedule.getPaymentScheduleDate() != null) {
            paymentScheduleDetailRp.setPaymentDate(DateUtil.format(DateUtil.YYYY_MM_DD_HH_MM_SS, paymentSchedule.getPaymentScheduleDate()));
        }
        return paymentScheduleDetailRp;
    }
}
