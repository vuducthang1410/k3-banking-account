package com.example.notification_service.service.interfaces;

import com.system.common_library.dto.notifcation.BalanceFluctuationNotificationDTO;
import com.system.common_library.dto.notifcation.OTP;
import com.system.common_library.dto.notifcation.rabbitMQ.*;
import com.system.common_library.dto.response.transaction.TransactionNotificationDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;


public interface HandleEventService{
    boolean sendOTP(OTP otp, CustomerDetailDTO customer) ;
    boolean sendOTP(OTP otp, String customerCIF);
    boolean sendTransactionNotification(TransactionNotificationDTO transaction)  ;
    boolean sendBalanceFluctuation(BalanceFluctuationNotificationDTO balanceFluctuationNotificationDTO) ;
    boolean sendWelcomeCustomerMessage(CustomerDetailDTO customerDetailDTO);
    boolean sendTransactionSuspicious(TransactionSuspiciousNoti transactionSuspicious) ;
    boolean sendUpdateCustomerInformation(CustomerDetailDTO customerDetail);
    boolean sendAccountLoanRegisterSuccessful(LoanAccountNoti loanAccountNoti);
    boolean sendAccountSavingRegister(SavingAccountNoti savingAccount);

    boolean sendAccountPaymentRegisterSuccessful(PaymentAccountNoti paymentAccount);

    boolean sendLoanReminder(LoanReminderNoti loanReminder);

    boolean sendLoanFinancialReviewFail(LoanFinancialReviewFailNoti loanFinancialReviewFail);

    boolean sendLoanFinancialReviewSuccess(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccess);

    boolean sendLoanCompletion(LoanCompletionNoti loanCompletion);

    boolean sendOverdueDept(LoanOverDueNoti loanOverDue);

    boolean sendDisbursementFail(String customerCIF);

    boolean sendDisbursementSuccess(LoanDisbursementSuccessNoti loandisbursementsuccess);

    boolean sendLoanPaymentSuccess(LoanPaymentSuccessNoti loanPaymentSuccessNoti);
}