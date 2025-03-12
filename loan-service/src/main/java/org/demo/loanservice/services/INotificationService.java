package org.demo.loanservice.services;

import com.system.common_library.dto.notifcation.rabbitMQ.*;

public interface INotificationService {
    void sendNotificationLoanFinancialReviewSuccess(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccessNoti);
    void sendNotificationLoanFinancialReviewFailure(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccessNoti);
    void sendNotificationDisbursementSuccess(LoanDisbursementSuccessNoti loanDisbursementSuccessNoti);
    void sendNotificationOverdue(LoanOverDueNoti loanOverDueNoti);
    void sendNotificationLoanReminder(LoanReminderNoti loanReminderNoti);
    void sendNotificationLoanComplete(LoanCompletionNoti loanCompletionNoti);
    void sendNotificationApprovedLoanSuccess(LoanAccountNoti loanAccountNoti);
}
