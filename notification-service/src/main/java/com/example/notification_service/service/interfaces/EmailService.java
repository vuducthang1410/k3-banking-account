package com.example.notification_service.service.interfaces;

import com.example.notification_service.domain.enumValue.Template;
import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.notifcation.rabbitMQ.*;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;

import java.util.Map;

public interface EmailService {
    boolean sendOTPAuthentication(OTP otp, String customerfullname, String email) ;
    boolean sendTransactionSuccess(TransactionNotificationDTO transaction, String customerfullname, String email) ;
    boolean processSendEmail(String email, Map<String, Object> variables, Template event) ;
    boolean sendTransactionFail(TransactionNotificationDTO transaction, String customerfullname, String email) ;
    boolean sendWelcomeCustomerMessage(String mail, String fullName, String phone) ;
    boolean sendTransactionSuspicious(TransactionSuspiciousNoti transactionSuspicious, String customerfullname, String email) ;
    boolean sendCUstomerUpdateInformation(CustomerDetailDTO customerDetail);
    public boolean sendAccountLoanRegisterSuccessful(LoanAccountNoti loanAccountNoti, String customerfullname, String email);

    boolean sendAccountSavingRegisterSuccessful(SavingAccountNoti savingAccount, String customerfullname, String email);

    boolean sendAccountPaymentRegisterSuccessful(PaymentAccountNoti paymentAccount, String customerfullname, String email);

    boolean sendLoanReminder(LoanReminderNoti loanReminder, String fullName, String mail);

    boolean sendLoanFinancialReviewFail(LoanFinancialReviewFailNoti loanFinancialReviewFail, String fullName, String mail);

    boolean sendLoanFinancialReviewSuccess(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccess, String fullName, String mail);

    boolean sendLoanCompletion(LoanCompletionNoti loanCompletion, String fullName, String mail);

    boolean sendOverdueDept(LoanOverDueNoti loanOverDue, String fullName, String mail);

    boolean sendDisbursementFail(String fullName, String mail);

    boolean sendDisbursementSuccess(LoanDisbursementSuccessNoti loandisbursementsuccess, String fullName, String mail);

    boolean sendLoanPaymentSuccess(LoanPaymentSuccessNoti loanPaymentSuccessNoti, String fullName, String mail);
}
