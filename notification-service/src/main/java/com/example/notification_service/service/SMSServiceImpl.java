package com.example.notification_service.service;

import com.example.notification_service.client.SMSFeignClient;
import com.example.notification_service.domain.dto.MessageDTO;
import com.example.notification_service.domain.dto.MessageRequestDTO;
import com.example.notification_service.domain.entity.NotificationTemplate;
import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Status;
import com.example.notification_service.domain.enumValue.Template;
import com.example.notification_service.service.interfaces.NotificationTemplateService;
import com.example.notification_service.service.interfaces.SMSService;
import com.example.notification_service.service.interfaces.TrackingNotificationService;
import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.notifcation.rabbitMQ.*;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SMSServiceImpl implements SMSService {
    private final ObjectConverterImpl objectConverter;
    private final NotificationTemplateService notificationTemplateService;
    private final TemplateEngine textTemplateEngine;
    private final SMSFeignClient smsFeignClient;
    private final TrackingNotificationService trackingNotificationService;

    @Value("${sms.service.sender.phone}")
    private String SENDER_PHONE_NUMBER;

    @Override
    public boolean sendOTPAuthentication(OTP otp, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(otp);
        variables.put("expiredTime", objectConverter.getDateTime(otp.getExpiredTime()));
        return getaBoolean(phoneNumber, variables, Template.OTP_VERIFICATION);
    }
    @Override
    public boolean sendTransactionSuccess(TransactionNotificationDTO transaction, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(transaction);
        variables.put("balance", objectConverter.getMoneyFormat(transaction.getBalance()));
        variables.put("debitAmount", objectConverter.getMoneyFormat(transaction.getDebitAmount()));
        variables.put("transactionDate", objectConverter.getDate(transaction.getTransactionDate()));
        variables.put("fee", objectConverter.getMoneyFormat(transaction.getFee()));
        return getaBoolean(phoneNumber, variables, Template.TRANSACTION_SUCCESS);
    }
    @Override
    public boolean sendTransactionFail(TransactionNotificationDTO transaction, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(transaction);
        variables.put("balance", objectConverter.getMoneyFormat(transaction.getBalance()));
        variables.put("debitAmount", objectConverter.getMoneyFormat(transaction.getDebitAmount()));
        variables.put("transactionDate", objectConverter.getDate(transaction.getTransactionDate()));
        variables.put("fee", objectConverter.getMoneyFormat(transaction.getFee()));
        return getaBoolean(phoneNumber, variables, Template.TRANSACTION_FAIL);
    }

    // Phone number validation method using regex
    private boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^[0-9]{10,15}$"; // Example: Allows 10 to 15 digit phone numbers
        return phoneNumber != null && phoneNumber.matches(phoneRegex);
    }

    private boolean getaBoolean(String phoneNumber, Map<String, Object> variables, Template event) {
        try {
            // Input validation
            if (!isValidPhoneNumber(phoneNumber)) {
                log.error("Invalid phone number: {}", phoneNumber);
                trackingNotificationService.saveTracking(null, Status.Fail, "Invalid phone number: "+ phoneNumber);
                return false;
            }
            Optional<NotificationTemplate> template = notificationTemplateService.retrieveTemplate(event, Channel.SMS);
            if (template.isEmpty()) {
                log.error("No SMS template found for event: {}", event);
                trackingNotificationService.saveTracking(null, Status.Fail, "No SMS template found for event: "+ event+ " channel "+Channel.SMS);
                return false;
            }
            // Prepare SMS content
            NotificationTemplate notificationTemplate = template.get();
            Context context = new Context();
            context.setVariables(variables);
            String message = textTemplateEngine.process(notificationTemplate.getContent(), context);
            if (message == null || message.trim().isEmpty()) {
                log.error("Generated SMS message is empty");
                trackingNotificationService.saveTracking(notificationTemplate, Status.Fail, "Generated SMS message is empty ");
                return false;
            }
            log.info("Generated SMS Message: {}", message);
            if (sendMessageToSMSHost(notificationTemplate, message, phoneNumber)) {
                return true;
            } else {
                log.error("Failed to send SMS to: {}", phoneNumber);
                trackingNotificationService.saveTracking(notificationTemplate, Status.Fail, "Failed to send SMS to " + phoneNumber+" by internal error");
                return false;
            }

        } catch (Exception e) {
            log.error("Error occurred while processing SMS sending: {}", e.getMessage(), e);
            trackingNotificationService.saveTracking(null, Status.Fail, "Error occurred while processing SMS sending: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendMessageToSMSHost( NotificationTemplate template, String message, String phoneNumber) {
        try {
            // Prepare the request payload
            MessageRequestDTO messageRequeest = MessageRequestDTO
                .builder()
                .receiverPhonenumber(phoneNumber)
                .message(message)
                .senderPhonenumber(SENDER_PHONE_NUMBER)
                .build();
            log.info("Final SMS Content: {}", message);
            // Send SMS via Feign client
            ResponseEntity<MessageDTO> response = smsFeignClient.sendSms(messageRequeest);
            // Validate response
            if (response == null || response.getStatusCode().isError()) {
//                trackingNotificationService.saveTracking(null, Status.Failed, "Failed to send SMS. Response is null or indicates an error.");
                log.error("Failed to send SMS. Response is null or indicates an error.");

                return false;
            }

            MessageDTO messageResponse = response.getBody();
            log.info("Final SMS Response: {}", messageResponse);
            log.info("SMS sent successfully to {}", phoneNumber);
            trackingNotificationService.saveTracking(template, Status.Success, "SMS sent successfully to  "+ phoneNumber);
            return true;
        } catch (FeignException fe) {
//            trackingNotificationService.saveTracking(null, Status.Failed, "FeignException while sending SMS:  "+ fe.getMessage());
            log.error("FeignException while sending SMS: {}", fe.getMessage(), fe);
            System.err.println("FeignException while sending SMS: " + fe.getMessage());

            return false;
        } catch (Exception e) {
//            trackingNotificationService.saveTracking(null, Status.Failed, "Unexpected error while sending SMS: "+ e.getMessage());
            log.error("Unexpected error while sending SMS: {}", e.getMessage(), e);
            System.err.println("Unexpected error while sending SMS: "+ e.getMessage());

            return false;
        }
    }

    @Override
    public boolean sendWelcomeCustomerMessage(String fullName, String phoneNumber) {
        Map<String,Object> variables = new HashMap<>();
        variables.put("customerfullname", fullName);
        variables.put("phoneNumber", phoneNumber);
        return getaBoolean(phoneNumber, variables, Template.CUSTOMER_WELCOME);
    }

    @Override
    public boolean sendTransactionSuspicious(TransactionSuspiciousNoti transactionSuspicious, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(transactionSuspicious);
        variables.put("transactionTime", objectConverter.getDateTime(transactionSuspicious.getTransactionTime()));
        variables.put("transactionAmount", objectConverter.getMoneyFormat(transactionSuspicious.getTransactionAmount()));
        return getaBoolean(phoneNumber, variables, Template.TRANSACTION_SUSPICIOUS);
    }

    @Override
    public boolean sendCUstomerUpdateInformation(CustomerDetailDTO customerDetail) {
        Map<String,Object> variables = new HashMap<>();
        variables.put("phoneNumber", customerDetail.getPhone());
        variables.put("address", customerDetail.getAddress());
        variables.put("emailAddress", customerDetail.getMail());
        return getaBoolean(customerDetail.getPhone(), variables, Template.CUSTOMER_INFORMATION_UPDATE);
    }

    @Override
    public boolean sendAccountLoanRegisterSuccessful(LoanAccountNoti loanAccountNoti, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(loanAccountNoti);
        variables.put("loanDueAmount", objectConverter.getMoneyFormat(loanAccountNoti.getLoanDueAmount()));
        variables.put("openDate", objectConverter.getDate(loanAccountNoti.getOpenDate()));
        variables.put("loanDueDate", objectConverter.getDate(loanAccountNoti.getLoanDueDate()));
        return getaBoolean(phoneNumber, variables, Template.ACCOUNT_LOAN_REGISTER_SUCCESSFUL);
    }

    @Override
    public boolean sendAccountSavingRegisterSuccessful(SavingAccountNoti savingAccount, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(savingAccount);
        variables.put("loanDueAmount", objectConverter.getMoneyFormat(savingAccount.getDepositAmount()));
        variables.put("openDate", objectConverter.getDate(savingAccount.getOpenDate()));
        variables.put("interestRate", objectConverter.getPercentageFormat(savingAccount.getInterestRate()));
        return getaBoolean(phoneNumber, variables, Template.ACCOUNT_SAVINGS_REGISTER_SUCCESSFUL);
    }

    @Override
    public boolean sendAccountPaymentRegisterSuccessful(PaymentAccountNoti paymentAccount, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(paymentAccount);
        variables.put("openDate", objectConverter.getDate(paymentAccount.getOpenDate()));
        return getaBoolean(phoneNumber, variables, Template.ACCOUNT_PAYMENT_REGISTER_SUCCESSFUL);
    }

    @Override
    public boolean sendLoanReminder(LoanReminderNoti loanReminder, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(loanReminder);
        variables.put("dueDate", objectConverter.getDate(loanReminder.getDueDate()));
        variables.put("amountDue", objectConverter.getMoneyFormat(loanReminder.getAmountDue()));
        return getaBoolean(phoneNumber, variables, Template.LOAN_REMIND_DEADLINE);
    }

    @Override
    public boolean sendLoanFinancialReviewFail(LoanFinancialReviewFailNoti loanFinancialReviewFail, String phoneNumber) {
        Map<String,Object> variables = new HashMap<>();
        variables.put("expiryDate", objectConverter.getDate(loanFinancialReviewFail.getExpiryDate()));
        return getaBoolean(phoneNumber, variables, Template.LOAN_FINANCIAL_REVIEW_FAIL);
    }

    @Override
    public boolean sendLoanFinancialReviewSuccess(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccess, String phoneNumber) {
        Map<String,Object> variables = new HashMap<>();
        variables.put("expiryDate", objectConverter.getDate(loanFinancialReviewSuccess.getExpiryDate()));
        variables.put("approvedLimit", objectConverter.getMoneyFormat(loanFinancialReviewSuccess.getApprovedLimit()));
        return getaBoolean(phoneNumber, variables, Template.LOAN_FINANCIAL_REVIEW_SUCCESS);
    }

    @Override
    public boolean sendLoanCompletion(LoanCompletionNoti loanCompletion, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(loanCompletion);
        variables.put("settlementDate", objectConverter.getDate(loanCompletion.getSettlementDate()));
        variables.put("amountPaid", objectConverter.getMoneyFormat(loanCompletion.getAmountPaid()));
        return getaBoolean(phoneNumber, variables, Template.LOAN_COMPLETION);
    }

    @Override
    public boolean sendOverdueDept(LoanOverDueNoti loanOverDue, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(loanOverDue);
        variables.put("dueDate", objectConverter.getDate(loanOverDue.getDueDate()));
        variables.put("amountDue", objectConverter.getMoneyFormat(loanOverDue.getAmountDue()));
        return getaBoolean(phoneNumber, variables, Template.LOAN_OVERDUE_DEPT);

    }

    @Override
    public boolean sendDisbursementFail(String phoneNumber) {
        Map<String,Object> variables = new HashMap<>();
        return getaBoolean(phoneNumber, variables, Template.LOAN_DISBURSEMENT_FAIL);
    }

    @Override
    public boolean sendDisbursementSuccess(LoanDisbursementSuccessNoti loandisbursementsuccess, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(loandisbursementsuccess);
        variables.put("disbursementDate", objectConverter.getDate(loandisbursementsuccess.getDisbursementDate()));
        variables.put("loanAmount", objectConverter.getMoneyFormat(loandisbursementsuccess.getLoanAmount()));
        return getaBoolean(phoneNumber, variables, Template.LOAN_DISBURSEMENT_SUCCESS);

    }

    @Override
    public boolean sendLoanPaymentSuccess(LoanPaymentSuccessNoti loanPaymentSuccessNoti, String phoneNumber) {
        Map<String,Object> variables = objectConverter.convertToMap(loanPaymentSuccessNoti);
        variables.put("paymentDate", objectConverter.getDate(loanPaymentSuccessNoti.getPaymentDate()));
        variables.put("paymentAmount", objectConverter.getMoneyFormat(loanPaymentSuccessNoti.getPaymentAmount()));
        return getaBoolean(phoneNumber, variables, Template.LOAN_PAYMENT_SUCCESSFUL);
    }
}
