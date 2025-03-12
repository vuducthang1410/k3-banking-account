package com.example.notification_service.service.interfaces;

import com.example.notification_service.domain.entity.NotificationTemplate;
import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.notifcation.rabbitMQ.*;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;

public interface SMSService {
    boolean sendOTPAuthentication(OTP otp, String phoneNumber) ;
    boolean sendTransactionSuccess(TransactionNotificationDTO transaction, String phoneNumber) ;
    boolean sendTransactionFail(TransactionNotificationDTO transaction, String phoneNumber) ;
    boolean sendMessageToSMSHost(NotificationTemplate template, String message, String phoneNumber);
    boolean sendWelcomeCustomerMessage(String fullname, String phoneNumber);
    boolean sendTransactionSuspicious(TransactionSuspiciousNoti transactionSuspicious,String phoneNumber);

    boolean sendCUstomerUpdateInformation(CustomerDetailDTO customerDetail);

    boolean sendAccountLoanRegisterSuccessful(LoanAccountNoti loanAccountNoti, String phoneNumber);

    boolean sendAccountSavingRegisterSuccessful(SavingAccountNoti savingAccount, String phoneNumber);

    boolean sendAccountPaymentRegisterSuccessful(PaymentAccountNoti paymentAccount, String phoneNumber);

    boolean sendLoanReminder(LoanReminderNoti loanReminder, String phone);

    boolean sendLoanFinancialReviewFail(LoanFinancialReviewFailNoti loanFinancialReviewFail, String phone);

    boolean sendLoanFinancialReviewSuccess(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccess, String phone);

    boolean sendLoanCompletion(LoanCompletionNoti loanCompletion, String phone);

    boolean sendOverdueDept(LoanOverDueNoti loanOverDue, String phone);

    boolean sendDisbursementFail(String phone);

    boolean sendDisbursementSuccess(LoanDisbursementSuccessNoti loandisbursementsuccess, String phone);

    boolean sendLoanPaymentSuccess(LoanPaymentSuccessNoti loanPaymentSuccessNoti, String phone);
}
