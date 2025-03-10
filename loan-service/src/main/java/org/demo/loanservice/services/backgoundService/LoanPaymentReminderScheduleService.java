package org.demo.loanservice.services.backgoundService;

import com.system.common_library.dto.response.account.AccountInfoDTO;
import com.system.common_library.dto.transaction.loan.CreateLoanTransactionDTO;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.TransactionDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.dto.enumDto.DeftRepaymentStatus;
import org.demo.loanservice.dto.enumDto.PaymentTransactionType;
import org.demo.loanservice.dto.enumDto.PaymentType;
import org.demo.loanservice.dto.request.DeftRepaymentRq;
import org.demo.loanservice.entities.LoanPenalties;
import org.demo.loanservice.entities.PaymentSchedule;
import org.demo.loanservice.repositories.LoanPenaltiesRepository;
import org.demo.loanservice.repositories.PaymentScheduleRepository;
import org.demo.loanservice.services.IPaymentScheduleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanPaymentReminderScheduleService {

    private final PaymentScheduleRepository paymentScheduleRepository;
    private final IPaymentScheduleService paymentScheduleService;
    private final LoanPenaltiesRepository loanPenaltiesRepository;
    @DubboReference
    private AccountDubboService accountDubboService;
    @DubboReference
    private TransactionDubboService transactionDubboService;
    private static final Logger log = LogManager.getLogger(LoanPaymentReminderScheduleService.class);

    /**
     * Automatically processes due loan payments.
     * Runs based on the cron expression configured in application properties.
     */
    @Scheduled(cron = "${spring.cron-job.auto-deft-repayment}")
    private void automaticallyDeftRepayment() {
        log.info("Starting automatic loan repayment process...");

        Timestamp threeDaysAgo = Timestamp.valueOf(LocalDate.now().minusDays(3).atStartOfDay());
        List<PaymentSchedule> paymentSchedules = paymentScheduleRepository.findByIsDeletedFalseAndIsPaidFalseAndDueDate(threeDaysAgo);

        if (paymentSchedules.isEmpty()) {
            log.info("No suitable loan found for automatic payment.");
            return;
        }

        log.info("Processing {} loan(s) for automatic payment.", paymentSchedules.size());
        paymentSchedules.forEach(paymentSchedule -> {
            DeftRepaymentRq repaymentRequest = DeftRepaymentRq.builder()
                    .paymentScheduleId(paymentSchedule.getId())
                    .paymentType(PaymentType.ALL.name())
                    .build();

            log.debug("Initiating payment for schedule ID: {}", paymentSchedule.getId());
            paymentScheduleService.automaticallyRepaymentDeftPeriodically(repaymentRequest, "system automatically");
        });

        log.info("Automatic loan repayment process completed.");
    }

    /**
     * Notifies users about upcoming due payments one day in advance.
     */
    @Scheduled(cron = "${spring.cron-job.notify-up-coming-repayment}")
    private void notifyUpcomingDuePayments() {
        log.info("Checking for upcoming due payments...");

        Timestamp oneDayAhead = Timestamp.valueOf(LocalDate.now().plusDays(1).atStartOfDay());
        List<PaymentSchedule> upcomingPayments = paymentScheduleRepository.findByIsDeletedFalseAndIsPaidFalseAndDueDate(oneDayAhead);

        if (upcomingPayments.isEmpty()) {
            log.info("No upcoming due loan found for notification.");
            return;
        }

        log.info("Notifying {} user(s) about upcoming due payments.", upcomingPayments.size());
        upcomingPayments.forEach(paymentSchedule -> {
            log.debug("Loan ID: {}, Due Date: {}", paymentSchedule.getLoanDetailInfo().getDisbursementInfoHistory().getLoanAccountId(), paymentSchedule.getDueDate());
            CreateLoanTransactionDTO loanTransactionDTO = new CreateLoanTransactionDTO();
            transactionDubboService.createLoanTransaction(loanTransactionDTO);
        });

        log.info("Upcoming due payment notifications sent.");
    }

    /**
     * Processes overdue loans and applies penalties if necessary.
     */
    @Scheduled(cron = "${spring.cron-job.process-overdue-payments}")
    private void processOverduePayments() {
        log.info("Checking for overdue loans...");

        Timestamp fourDaysAgo = Timestamp.valueOf(LocalDate.now().minusDays(3).atStartOfDay());
        List<PaymentSchedule> overduePayments = paymentScheduleRepository.findByIsDeletedFalseAndIsPaidFalseAndDueDateBefore(fourDaysAgo);

        if (overduePayments.isEmpty()) {
            log.info("No overdue loan found for penalty enforcement.");
            return;
        }

        log.info("Applying penalties for {} overdue loan(s).", overduePayments.size());
        List<LoanPenalties> penaltiesList = new ArrayList<>();

        overduePayments.forEach(paymentSchedule -> {
            LoanPenalties penalty = new LoanPenalties();
            penalty.setFinedPaymentDate(new Date());
            penalty.setIsPaid(false);
            penalty.setFinedReason(DeftRepaymentStatus.OVERDUE.name());
            penalty.setPaymentSchedule(paymentSchedule);

            // Calculate overdue penalty based on the loan amount and interest rate
            BigDecimal fineAmount = paymentSchedule.getAmountRepayment()
                    .multiply(BigDecimal.valueOf(paymentSchedule.getLoanDetailInfo().getInterestRate()))
                    .multiply(new BigDecimal("1.5"));
            penalty.setFinedAmount(fineAmount);
            penaltiesList.add(penalty);

            AccountInfoDTO accountLoanInfoDTO = accountDubboService.getLoanAccountDTO(paymentSchedule.getLoanDetailInfo().getDisbursementInfoHistory().getLoanAccountId());
            CreateLoanTransactionDTO loanTransactionDTO = new CreateLoanTransactionDTO();
            loanTransactionDTO.setAmount(fineAmount);
            loanTransactionDTO.setDescription(PaymentType.PENALTY.name());
            loanTransactionDTO.setCifCode(paymentSchedule.getLoanDetailInfo().getFinancialInfo().getCifCode());
            loanTransactionDTO.setLoanAccount(accountLoanInfoDTO.getAccountNumber());
            loanTransactionDTO.setNote(PaymentTransactionType.OVERDUE_PAYMENT_PENALTY.name());
            transactionDubboService.createLoanTransaction(loanTransactionDTO);

            log.debug("Penalty applied: Loan ID: {},Amount:{},Interest rate: {}, Fine Amount: {}",
                    paymentSchedule.getLoanDetailInfo().getDisbursementInfoHistory().getLoanAccountId(),
                    paymentSchedule.getAmountRepayment(),
                    paymentSchedule.getLoanDetailInfo().getInterestRate(),
                    fineAmount);
        });

        loanPenaltiesRepository.saveAll(penaltiesList);
        log.info("Overdue penalties have been successfully applied.");
    }
}
