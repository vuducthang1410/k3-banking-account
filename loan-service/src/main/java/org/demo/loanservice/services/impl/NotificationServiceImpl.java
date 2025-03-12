package org.demo.loanservice.services.impl;

import com.system.common_library.dto.notifcation.rabbitMQ.*;
import lombok.RequiredArgsConstructor;
import org.demo.loanservice.services.INotificationService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {
    // Routing Keys
    @Value("${rabbitmq.routing.key.disbursement.success}")
    private String routingDisbursementSuccess;

    @Value("${rabbitmq.routing.key.disbursement.fail}")
    private String routingDisbursementFail;

    @Value("${rabbitmq.routing.key.loan.approved_success}")
    private String routingLoanApprovedSuccess;

    @Value("${rabbitmq.routing.key.loan.approved_fail}")
    private String routingLoanApprovedFail;

    @Value("${rabbitmq.routing.key.loan.remind}")
    private String routingLoanRemind;

    @Value("${rabbitmq.routing.key.loan.completion}")
    private String routingLoanCompletion;

    @Value("${rabbitmq.routing.key.loan.overdue_debt_loan}")
    private String routingLoanOverdueDebt;

    @Value("${rabbitmq.routing.key.loan.create_account_loan}")
    private String routingLoanCreateAccount;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendNotificationLoanFinancialReviewSuccess(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccessNoti) {
        rabbitTemplate.convertAndSend(exchangeName,routingLoanApprovedSuccess, loanFinancialReviewSuccessNoti);
    }

    @Override
    public void sendNotificationLoanFinancialReviewFailure(LoanFinancialReviewSuccessNoti loanFinancialReviewSuccessNoti) {
        rabbitTemplate.convertAndSend(exchangeName,routingLoanApprovedFail, loanFinancialReviewSuccessNoti);
    }

    @Override
    public void sendNotificationDisbursementSuccess(LoanDisbursementSuccessNoti loanDisbursementSuccessNoti) {
        rabbitTemplate.convertAndSend(exchangeName, routingDisbursementSuccess, loanDisbursementSuccessNoti);
    }

    @Override
    public void sendNotificationOverdue(LoanOverDueNoti loanOverDueNoti) {
        rabbitTemplate.convertAndSend(exchangeName, routingLoanOverdueDebt, loanOverDueNoti);
    }

    @Override
    public void sendNotificationLoanReminder(LoanReminderNoti loanReminderNoti) {
        rabbitTemplate.convertAndSend(exchangeName, routingLoanRemind, loanReminderNoti);
    }

    @Override
    public void sendNotificationLoanComplete(LoanCompletionNoti loanCompletionNoti) {
        rabbitTemplate.convertAndSend(exchangeName, routingLoanCompletion, loanCompletionNoti);
    }

    @Override
    public void sendNotificationApprovedLoanSuccess(LoanAccountNoti loanAccountNoti) {
        rabbitTemplate.convertAndSend(exchangeName,routingLoanApprovedSuccess, loanAccountNoti);
    }
}
