package com.example.notification_service.service;

import com.example.notification_service.domain.entity.NotificationTemplate;
import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Status;
import com.example.notification_service.domain.enumValue.Template;
import com.example.notification_service.service.interfaces.*;
import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.notifcation.rabbitMQ.*;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final ObjectConverter objectConverter;
    private final EmailSendingService emailSendingService;
    @Qualifier("htmlTemplateEngine")
    private final TemplateEngine htmlTemplateEngine;
    private final NotificationTemplateService notificationTemplateService;
    private final TrackingNotificationService trackingNotificationService;
    // Email validation method
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }
    @Override
    public boolean processSendEmail(String email, Map<String, Object> variables, Template event)  {
        try {

            // Validate email and template
            Optional<NotificationTemplate> template = notificationTemplateService.retrieveTemplate(event, Channel.EMAIL);
            if (template.isEmpty()) {
                log.error("No template found for event: {}", event);
                trackingNotificationService.saveTracking(null, Status.Fail, "No template found for event "+ event+" channel "+ Channel.EMAIL);
                return false;
            }
            else if (email == null || email.isEmpty() || !isValidEmail(email)) {
                log.error("Invalid email address: {}", email);
                trackingNotificationService.saveTracking(template.get(), Status.Fail, "Invalid email address: "+ email);
                return false;
            }
            NotificationTemplate notificationTemplate = template.get();
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = htmlTemplateEngine.process(notificationTemplate.getContent(), context);
            String subject = notificationTemplate.getTitle();
            if (emailSendingService.sendHTMLEmail(email, subject, htmlContent)) {
                trackingNotificationService.saveTracking(template.get(), Status.Success, "Email sent successfully to "+ email);
                return true;
            } else {
                log.error("Failed to send email to: {}", email);
                trackingNotificationService.saveTracking(notificationTemplate, Status.Fail, "Invalid email address: "+ email);
                return false;
            }
        }
        catch (Exception e) {
            log.error("Error occurred while processing email sending: {}", e.getMessage(), e);
            trackingNotificationService.saveTracking(null, Status.Fail, "Error occurred while processing email sending: {}"+ e.getMessage());
            return false;
        }
    }
    @Override
    public boolean sendOTPAuthentication(OTP otp, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(otp);
        variables.put("customerfullname", customerfullname);
        variables.put("expiredTime", objectConverter.getDateTime(otp.getExpiredTime()));
        return processSendEmail(email, variables, Template.OTP_VERIFICATION);
    }
    @Override
    public boolean sendTransactionSuccess(TransactionNotificationDTO transaction, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(transaction);
        variables.put("balance", objectConverter.getMoneyFormat(transaction.getBalance()));
        variables.put("debitAmount", objectConverter.getMoneyFormat(transaction.getDebitAmount()));
        variables.put("customerfullname", customerfullname);
        variables.put("transactionDate", objectConverter.getDate(transaction.getTransactionDate()));
        variables.put("fee", objectConverter.getMoneyFormat(transaction.getFee()));
        return processSendEmail(email, variables, Template.TRANSACTION_SUCCESS);
    }
    @Override
    public boolean sendTransactionFail(TransactionNotificationDTO transaction, String customerfullname, String email)  {
        Map<String,Object> variables = objectConverter.convertToMap(transaction);
        variables.put("balance", objectConverter.getMoneyFormat(transaction.getBalance()));
        variables.put("debitAmount", objectConverter.getMoneyFormat(transaction.getDebitAmount()));
        variables.put("customerfullname", customerfullname);
        variables.put("transactionDate", objectConverter.getDate(transaction.getTransactionDate()));
        variables.put("fee", objectConverter.getMoneyFormat(transaction.getFee()));
        return processSendEmail(email, variables, Template.TRANSACTION_FAIL);
    }
    @Override
    public boolean sendWelcomeCustomerMessage(String email, String fullName, String phoneNumber) {
        Map<String,Object> variables = new HashMap<>();
        variables.put("customerfullname", fullName);
        variables.put("phoneNumber", phoneNumber);
        return processSendEmail(email, variables, Template.CUSTOMER_WELCOME);
    }
    @Override
    public boolean sendTransactionSuspicious(TransactionSuspiciousNoti transactionSuspicious, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(transactionSuspicious);
        variables.put("customerfullname", customerfullname);
        variables.put("transactionTime", objectConverter.getDateTime(transactionSuspicious.getTransactionTime()));
        variables.put("transactionAmount", objectConverter.getMoneyFormat(transactionSuspicious.getTransactionAmount()));
        return processSendEmail(email, variables, Template.TRANSACTION_SUSPICIOUS);
    }

    @Override
    public boolean sendCUstomerUpdateInformation(CustomerDetailDTO customerDetail) {
        Map<String,Object> variables = new HashMap<>();
        variables.put("customerfullname", customerDetail.getFullName());
        variables.put("phoneNumber", customerDetail.getPhone());
        variables.put("address", customerDetail.getAddress());
        variables.put("emailAddress", customerDetail.getMail());
        return processSendEmail(customerDetail.getMail(), variables, Template.CUSTOMER_INFORMATION_UPDATE);
    }

    @Override
    public boolean sendAccountLoanRegisterSuccessful(LoanAccountNoti loanAccountNoti, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(loanAccountNoti);
        variables.put("customerfullname", customerfullname);
        variables.put("loanDueAmount", objectConverter.getMoneyFormat(loanAccountNoti.getLoanDueAmount()));
        variables.put("openDate", objectConverter.getDate(loanAccountNoti.getOpenDate()));
        variables.put("loanDueDate", objectConverter.getDate(loanAccountNoti.getLoanDueDate()));
        return processSendEmail(email, variables, Template.ACCOUNT_LOAN_REGISTER_SUCCESSFUL);
    }

    @Override
    public boolean sendAccountSavingRegisterSuccessful(SavingAccountNoti savingAccount, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(savingAccount);
        variables.put("loanDueAmount", objectConverter.getMoneyFormat(savingAccount.getDepositAmount()));
        variables.put("openDate", objectConverter.getDate(savingAccount.getOpenDate()));
        variables.put("interestRate", objectConverter.getPercentageFormat(savingAccount.getInterestRate()));
        variables.put("customerfullname", customerfullname);
        return processSendEmail(email, variables, Template.ACCOUNT_SAVINGS_REGISTER_SUCCESSFUL);
    }

    @Override
    public boolean sendAccountPaymentRegisterSuccessful(PaymentAccountNoti paymentAccount, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(paymentAccount);
        variables.put("openDate", objectConverter.getDate(paymentAccount.getOpenDate()));
        variables.put("customerfullname", customerfullname);
        return processSendEmail(email, variables, Template.ACCOUNT_PAYMENT_REGISTER_SUCCESSFUL);
    }

    @Override
    public boolean sendLoanReminder(LoanReminderNoti loanReminder, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(loanReminder);
        variables.put("dueDate", objectConverter.getDate(loanReminder.getDueDate()));
        variables.put("contractNumber", (loanReminder.getContractNumber()));
        variables.put("amountDue", objectConverter.getMoneyFormat(loanReminder.getAmountDue()));
        variables.put("customerfullname", customerfullname);
        return processSendEmail(email, variables, Template.LOAN_REMIND_DEADLINE);
    }

    @Override
    public boolean sendLoanFinancialReviewFail(LoanFinancialReviewFailNoti loanFinancialReviewFail, String customerfullname, String email) {
        Map<String,Object> variables = new HashMap<>();
        variables.put("expiryDate", objectConverter.getDate(loanFinancialReviewFail.getExpiryDate()));
        variables.put("customerfullname", customerfullname);
        return processSendEmail(email, variables, Template.LOAN_FINANCIAL_REVIEW_FAIL);
    }

    @Override
    public boolean sendLoanFinancialReviewSuccess(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccess, String customerfullname, String email) {
        Map<String,Object> variables = new HashMap<>();
        variables.put("expiryDate", objectConverter.getDate(loanFinancialReviewSuccess.getExpiryDate()));
        variables.put("customerfullname", customerfullname);
        variables.put("approvedLimit", objectConverter.getMoneyFormat(loanFinancialReviewSuccess.getApprovedLimit()));
        return processSendEmail(email, variables, Template.LOAN_FINANCIAL_REVIEW_SUCCESS);
    }

    @Override
    public boolean sendLoanCompletion(LoanCompletionNoti loanCompletion, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(loanCompletion);
        variables.put("settlementDate", objectConverter.getDate(loanCompletion.getSettlementDate()));
        variables.put("amountPaid", objectConverter.getMoneyFormat(loanCompletion.getAmountPaid()));
        variables.put("customerfullname", customerfullname);
        return processSendEmail(email, variables, Template.LOAN_COMPLETION);
    }

    @Override
    public boolean sendOverdueDept(LoanOverDueNoti loanOverDue, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(loanOverDue);
        variables.put("dueDate", objectConverter.getDate(loanOverDue.getDueDate()));
        variables.put("amountDue", objectConverter.getMoneyFormat(loanOverDue.getAmountDue()));
        variables.put("penaltyFee", objectConverter.getMoneyFormat(loanOverDue.getPenaltyFee()));
        variables.put("customerfullname", customerfullname);
        return processSendEmail(email, variables, Template.LOAN_OVERDUE_DEPT);
    }

    @Override
    public boolean sendDisbursementFail(String customerfullname, String email) {
        Map<String,Object> variables = new HashMap<>();
        variables.put("customerfullname", customerfullname);
        return processSendEmail(email, variables, Template.LOAN_DISBURSEMENT_FAIL);
    }

    @Override
    public boolean sendDisbursementSuccess(LoanDisbursementSuccessNoti loandisbursementsuccess, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(loandisbursementsuccess);
        variables.put("disbursementDate", objectConverter.getDate(loandisbursementsuccess.getDisbursementDate()));
        variables.put("loanAmount", objectConverter.getMoneyFormat(loandisbursementsuccess.getLoanAmount()));
        variables.put("customerfullname", customerfullname);
        return processSendEmail(email, variables, Template.LOAN_DISBURSEMENT_SUCCESS);
    }

    @Override
    public boolean sendLoanPaymentSuccess(LoanPaymentSuccessNoti loanPaymentSuccessNoti, String customerfullname, String email) {
        Map<String,Object> variables = objectConverter.convertToMap(loanPaymentSuccessNoti);
        variables.put("paymentDate", objectConverter.getDate(loanPaymentSuccessNoti.getPaymentDate()));
        variables.put("paymentAmount", objectConverter.getMoneyFormat(loanPaymentSuccessNoti.getPaymentAmount()));
        variables.put("customerfullname", customerfullname);
        return processSendEmail(email, variables, Template.LOAN_PAYMENT_SUCCESSFUL);
    }
}
