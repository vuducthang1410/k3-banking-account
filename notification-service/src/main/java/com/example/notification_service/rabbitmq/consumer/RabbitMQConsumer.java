package com.example.notification_service.rabbitmq.consumer;

import com.example.notification_service.service.interfaces.HandleEventService;
import com.system.common_library.dto.notifcation.rabbitMQ.*;
import com.system.common_library.dto.user.CustomerDetailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {
    private final HandleEventService handleEventService;

    @RabbitListener(queues = {"${rabbitmq.queue.consumer.customer.welcome}"})
    public boolean sendWelcomeCustomer(CustomerDetailDTO customerDetailDTO) {
      log.info("Welcome Customer Queue");
      log.info("Received message -> {}", customerDetailDTO.toString());
      return handleEventService.sendWelcomeCustomerMessage(customerDetailDTO);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.transaction.suspicious}"})
    public boolean sendTransactionSuspicious(TransactionSuspiciousNoti transactionSuspicious) {
        log.info("Transaction Suspicious Queue");
        log.info("Received message -> {}", transactionSuspicious.toString());
        return handleEventService.sendTransactionSuspicious(transactionSuspicious);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.customer.update}"})
    public boolean sendCustomerUpdateInformation(CustomerDetailDTO customerDetail ) {
        log.info("Customer Information Update Queue");
        log.info("Received message -> {}", customerDetail.toString());
        return handleEventService.sendUpdateCustomerInformation(customerDetail);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.account.create.savings}"})
    public boolean sendAccountSavingsRegisterSuccessful(SavingAccountNoti savingAccount) {
        log.info("Account Service Create Savings Account Successful Queue");
        log.info("Received message -> {}", savingAccount.toString());
        return handleEventService.sendAccountSavingRegister(savingAccount);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.account.create.payment}"})
    public boolean sendAccountPaymentRegisterSuccessful(PaymentAccountNoti paymentAccount) {
        log.info("Account Service Create Payment Account Successful Queue");
        log.info("Received message -> {}", paymentAccount.toString());
        return handleEventService.sendAccountPaymentRegisterSuccessful(paymentAccount);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.loan.create.account}"})
    public boolean sendAccountLoanRegisterSuccessful(LoanAccountNoti loanAccount) {
        log.info("Loan Service Create Loan Account Successful Queue");
        log.info("Received message -> {}", loanAccount.toString());
        return handleEventService.sendAccountLoanRegisterSuccessful(loanAccount);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.loan.approval.success}"})
    public boolean sendLoanFinancialReviewSuccess(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccess) {
        log.info("Loan Financial Review Success Queue");
        log.info("Received message -> {}", loanFinancialReviewSuccess.toString());
        return handleEventService.sendLoanFinancialReviewSuccess(loanFinancialReviewSuccess);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.loan.approval.fail}"})
    public boolean sendLoanFinancialReviewFail(LoanFinancialReviewFailNoti loanFinancialReviewFail) {
        log.info("Loan Financial Review Fail Queue");
        log.info("Received message -> {}", loanFinancialReviewFail.toString());
        return handleEventService.sendLoanFinancialReviewFail(loanFinancialReviewFail);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.loan.remind}"})
    public boolean sendLoanReminder(LoanReminderNoti loanReminder) {
        log.info("Loan Reminder Queue");
        log.info("Received message -> {}", loanReminder.toString());
        return handleEventService.sendLoanReminder(loanReminder);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.loan.completion}"})
    public boolean sendLoanCompletion(LoanCompletionNoti loanCompletion) {
        log.info("Loan Completion Queue");
        log.info("Received message -> {}", loanCompletion.toString());
        return handleEventService.sendLoanCompletion(loanCompletion);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.loan.overdue_dept}"})
    public boolean sendOverdueDept(LoanOverDueNoti loanOverDue) {
        log.info("Loan Completion Queue");
        log.info("Received message -> {}", loanOverDue.toString());
        return handleEventService.sendOverdueDept(loanOverDue);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.loan.disbursement.fail}"})
    public boolean sendDisbursementFail(String customerCIF) {
        log.info("Loan Completion Queue");
        log.info("Received message -> {}", customerCIF);
        return handleEventService.sendDisbursementFail(customerCIF);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.loan.disbursement.success}"})
    public boolean sendDisbursementSuccess(LoanDisbursementSuccessNoti loandisbursementsuccess) {
        log.info("Loan Completion Queue");
        log.info("Received message -> {}", loandisbursementsuccess.toString());
        return handleEventService.sendDisbursementSuccess(loandisbursementsuccess);
    }
    @RabbitListener(queues = {"${rabbitmq.queue.consumer.loan.payment.success}"})
    public boolean sendLoanPaymentSuccess(LoanPaymentSuccessNoti loanPaymentSuccessNoti) {
        log.info("Loan Completion Queue");
        log.info("Received message -> {}", loanPaymentSuccessNoti.toString());
        return handleEventService.sendLoanPaymentSuccess(loanPaymentSuccessNoti);
    }
}