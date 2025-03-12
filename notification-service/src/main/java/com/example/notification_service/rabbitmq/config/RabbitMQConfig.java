package com.example.notification_service.rabbitmq.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.consumer.customer.welcome}")
    private String customerWelcomeQueue;

    @Value("${rabbitmq.queue.consumer.transaction.suspicious}")
    private String transactionSuspiciousQueue;

    @Value("${rabbitmq.queue.consumer.customer.update}")
    private String customerUpdateQueue;

    @Value("${rabbitmq.queue.consumer.account.create.savings}")
    private String accountSavingsQueue;

    @Value("${rabbitmq.queue.consumer.account.create.payment}")
    private String accountPaymentQueue;

    @Value("${rabbitmq.queue.consumer.loan.create.account}")
    private String loanAccountQueue;

    @Value("${rabbitmq.queue.consumer.loan.approval.success}")
    private String loanApprovalSuccessQueue;

    @Value("${rabbitmq.queue.consumer.loan.approval.fail}")
    private String loanApprovalFailQueue;

    @Value("${rabbitmq.queue.consumer.loan.remind}")
    private String loanReminderQueue;

    @Value("${rabbitmq.queue.consumer.loan.completion}")
    private String loanCompletionQueue;

    @Value("${rabbitmq.queue.consumer.loan.overdue_dept}")
    private String loanOverdueDeptQueue;

    @Value("${rabbitmq.queue.consumer.loan.disbursement.fail}")
    private String loanDisbursementFailQueue;

    @Value("${rabbitmq.queue.consumer.loan.disbursement.success}")
    private String loanDisbursementSuccessQueue;
    @Value("${rabbitmq.queue.consumer.loan.payment.success}")
    private String loanPaymentSuccessQueue;
    @Bean
    public Queue loanPaymentSuccessQueue() {
        return new Queue(loanPaymentSuccessQueue);
    }

    @Bean
    public Queue customerWelcomeQueue() {
        return new Queue(customerWelcomeQueue);
    }

    @Bean
    public Queue transactionSuspiciousQueue() {
        return new Queue(transactionSuspiciousQueue);
    }

    @Bean
    public Queue customerUpdateQueue() {
        return new Queue(customerUpdateQueue);
    }

    @Bean
    public Queue accountSavingsQueue() {
        return new Queue(accountSavingsQueue);
    }

    @Bean
    public Queue accountPaymentQueue() {
        return new Queue(accountPaymentQueue);
    }

    @Bean
    public Queue loanAccountQueue() {
        return new Queue(loanAccountQueue);
    }

    @Bean
    public Queue loanApprovalSuccessQueue() {
        return new Queue(loanApprovalSuccessQueue);
    }

    @Bean
    public Queue loanApprovalFailQueue() {
        return new Queue(loanApprovalFailQueue);
    }

    @Bean
    public Queue loanReminderQueue() {
        return new Queue(loanReminderQueue);
    }

    @Bean
    public Queue loanCompletionQueue() {
        return new Queue(loanCompletionQueue);
    }

    @Bean
    public Queue loanOverdueDeptQueue() {
        return new Queue(loanOverdueDeptQueue);
    }

    @Bean
    public Queue loanDisbursementFailQueue() {
        return new Queue(loanDisbursementFailQueue);
    }

    @Bean
    public Queue loanDisbursementSuccessQueue() {
        return new Queue(loanDisbursementSuccessQueue);
    }


    @Bean
    public MessageConverter converter() {

        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ContainerCustomizer<SimpleMessageListenerContainer> containerCustomizer() {

        return container -> container.setObservationEnabled(true);
    }

    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
