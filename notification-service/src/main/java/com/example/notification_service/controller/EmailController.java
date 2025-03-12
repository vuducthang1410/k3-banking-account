package com.example.notification_service.controller;

import com.example.notification_service.service.interfaces.EmailService;
import com.example.notification_service.service.interfaces.HandleEventService;
import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.notifcation.rabbitMQ.*;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;
    private final HandleEventService handleEventService;

    @PostMapping("processOTP")
    public void processOTP() throws MessagingException {
//        log.info(request.toString());
        OTP otp = OTP.builder()
                .otp("123456")
                .expiredTime(LocalDateTime.now().plusMinutes(30))
                .build();
        String fullname = "Pham Ngoc Anh Thu";
        String email = "chirido0807@gmail.com";
        emailService.sendOTPAuthentication(otp, fullname, email);
    }
    @PostMapping("transaction-success")
    public void transactionSuccess() throws MessagingException {
        TransactionNotificationDTO transaction = TransactionNotificationDTO.builder()
                .customerCIF("123456")
                .balance(new BigDecimal("12000000"))
                .transactionType("transaction type")
                .transactionCode("CX12QW")
                .debitAccount("0703425730")
                .accountOwner("Pham Ngoc Anh Thu")
                .beneficiaryAccount("0703425730")
                .beneficiaryName("Nguyen Trinh Nhu Y")
                .beneficiaryBanK("MOMO")
                .transactionDate(LocalDateTime.now())
                .debitAmount(new BigDecimal("12000000"))
                .detailsOfTransaction("thank you for your help")
                .fee(new BigDecimal(0))
                .chargeType("fast charge")
                .isSuccess(true)
                .build();
        handleEventService.sendTransactionNotification(transaction);

    }
    @PostMapping("transaction-fail")
    public void transactionFail() throws MessagingException {
        TransactionNotificationDTO transaction = TransactionNotificationDTO.builder()
                .customerCIF("123456")
                .balance(new BigDecimal("12000000"))
                .transactionType("transaction type")
                .transactionCode("CX12QW")
                .debitAccount("0703425730")
                .accountOwner("Pham Ngoc Anh Thu")
                .beneficiaryAccount("0703425730")
                .beneficiaryName("Nguyen Trinh Nhu Y")
                .beneficiaryBanK("MOMO")
                .transactionDate(LocalDateTime.now())
                .debitAmount(new BigDecimal("12000000"))
                .detailsOfTransaction("thank you for your help")
                .fee(new BigDecimal(0))
                .chargeType("fast charge")
                .isSuccess(false)
                .build();
        handleEventService.sendTransactionNotification(transaction);
    }
    @PostMapping("customer-welcome")
    public void sendWelcomeCustomer(@RequestBody CustomerDetailDTO customerDetailDTO) {
        log.info("Received message -> {}", customerDetailDTO.toString());
        handleEventService.sendWelcomeCustomerMessage(customerDetailDTO);
    }

    @PostMapping("transaction-suspicious")
    public boolean sendTransactionSuspicious(@RequestBody TransactionSuspiciousNoti transactionSuspicious) {
        return handleEventService.sendTransactionSuspicious(transactionSuspicious);
    }
    @PostMapping("customer-update")
    public boolean sendCustomerUpdateInformation(@RequestBody CustomerDetailDTO customerDetail ) {
        return handleEventService.sendUpdateCustomerInformation(customerDetail);
    }
    @PostMapping("saving-account")
    public boolean sendAccountSavingsRegisterSuccessful(@RequestBody SavingAccountNoti savingAccount) {
        return handleEventService.sendAccountSavingRegister(savingAccount);
    }
    @PostMapping("payment-account")
    public boolean sendAccountPaymentRegisterSuccessful(@RequestBody PaymentAccountNoti paymentAccount) {
        return handleEventService.sendAccountPaymentRegisterSuccessful(paymentAccount);
    }
    @PostMapping("loan-account")
    public boolean sendAccountLoanRegisterSuccessful(@RequestBody LoanAccountNoti loanAccount) {
        return handleEventService.sendAccountLoanRegisterSuccessful(loanAccount);
    }
    @PostMapping("loan-finan-success")
    public boolean sendLoanFinancialReviewSuccess(@RequestBody LoanFinancialReviewSuccessNoti loanFinancialReviewSuccess) {
        return handleEventService.sendLoanFinancialReviewSuccess(loanFinancialReviewSuccess);
    }
    @PostMapping("loan-finan-fail")
    public boolean sendLoanFinancialReviewFail(@RequestBody LoanFinancialReviewFailNoti loanFinancialReviewFail) {
        return handleEventService.sendLoanFinancialReviewFail(loanFinancialReviewFail);
    }
    @PostMapping("loan-reminder")
    public boolean sendLoanReminder(@RequestBody LoanReminderNoti loanReminder) {
        return handleEventService.sendLoanReminder(loanReminder);
    }
    @PostMapping("loan-completion")
    public boolean sendLoanCompletion(@RequestBody LoanCompletionNoti loanCompletion) {
        return handleEventService.sendLoanCompletion(loanCompletion);
    }
    @PostMapping("loan-overdue")
    public boolean sendOverdueDept(@RequestBody LoanOverDueNoti loanOverDue) {
        return handleEventService.sendOverdueDept(loanOverDue);
    }
    @PostMapping("loan-disbursement-fail")
    public boolean sendDisbursementFail(@RequestBody String customerCIF) {
        return handleEventService.sendDisbursementFail(customerCIF);
    }
    @PostMapping("loan-disbursement-success")
    public boolean sendDisbursementSuccess(@RequestBody LoanDisbursementSuccessNoti loandisbursementsuccess) {
        return handleEventService.sendDisbursementSuccess(loandisbursementsuccess);
    }
    @PostMapping("loan-payment-success")
    public boolean sendLoanPaymentSuccess(@RequestBody LoanPaymentSuccessNoti loanPaymentSuccessNoti) {
        return handleEventService.sendLoanPaymentSuccess(loanPaymentSuccessNoti);
    }
}