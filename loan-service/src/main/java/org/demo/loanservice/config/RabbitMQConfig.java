package org.demo.loanservice.config;

import org.springframework.amqp.core.*;
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

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    // Disbursement Queues
    @Value("${rabbitmq.queue.name.disbursement.success}")
    private String disbursementSuccessQueue;

    @Value("${rabbitmq.queue.name.disbursement.fail}")
    private String disbursementFailQueue;

    // Loan Queues
    @Value("${rabbitmq.queue.name.loan.approved_success}")
    private String loanApprovedSuccessQueue;

    @Value("${rabbitmq.queue.name.loan.approved_fail}")
    private String loanApprovedFailQueue;

    @Value("${rabbitmq.queue.name.loan.remind}")
    private String loanRemindQueue;

    @Value("${rabbitmq.queue.name.loan.completion}")
    private String loanCompletionQueue;

    @Value("${rabbitmq.queue.name.loan.overdue_debt_loan}")
    private String loanOverdueDebtQueue;

    @Value("${rabbitmq.queue.name.loan.create_account_loan}")
    private String loanCreateAccountQueue;

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

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    // Queue Beans
    @Bean
    public Queue disbursementSuccessQueue() {
        return new Queue(disbursementSuccessQueue);
    }

    @Bean
    public Queue disbursementFailQueue() {
        return new Queue(disbursementFailQueue);
    }

    @Bean
    public Queue loanApprovedSuccessQueue() {
        return new Queue(loanApprovedSuccessQueue);
    }

    @Bean
    public Queue loanApprovedFailQueue() {
        return new Queue(loanApprovedFailQueue);
    }

    @Bean
    public Queue loanRemindQueue() {
        return new Queue(loanRemindQueue);
    }

    @Bean
    public Queue loanCompletionQueue() {
        return new Queue(loanCompletionQueue);
    }

    @Bean
    public Queue loanOverdueDebtQueue() {
        return new Queue(loanOverdueDebtQueue);
    }

    @Bean
    public Queue loanCreateAccountQueue() {
        return new Queue(loanCreateAccountQueue);
    }

    // Binding Beans
    @Bean
    public Binding bindingDisbursementSuccess() {
        return BindingBuilder.bind(disbursementSuccessQueue())
                .to(exchange())
                .with(routingDisbursementSuccess);
    }

    @Bean
    public Binding bindingDisbursementFail() {
        return BindingBuilder.bind(disbursementFailQueue())
                .to(exchange())
                .with(routingDisbursementFail);
    }

    @Bean
    public Binding bindingLoanApprovedSuccess() {
        return BindingBuilder.bind(loanApprovedSuccessQueue())
                .to(exchange())
                .with(routingLoanApprovedSuccess);
    }

    @Bean
    public Binding bindingLoanApprovedFail() {
        return BindingBuilder.bind(loanApprovedFailQueue())
                .to(exchange())
                .with(routingLoanApprovedFail);
    }

    @Bean
    public Binding bindingLoanRemind() {
        return BindingBuilder.bind(loanRemindQueue())
                .to(exchange())
                .with(routingLoanRemind);
    }

    @Bean
    public Binding bindingLoanCompletion() {
        return BindingBuilder.bind(loanCompletionQueue())
                .to(exchange())
                .with(routingLoanCompletion);
    }

    @Bean
    public Binding bindingLoanOverdueDebt() {
        return BindingBuilder.bind(loanOverdueDebtQueue())
                .to(exchange())
                .with(routingLoanOverdueDebt);
    }

    @Bean
    public Binding bindingLoanCreateAccount() {
        return BindingBuilder.bind(loanCreateAccountQueue())
                .to(exchange())
                .with(routingLoanCreateAccount);
    }

    // Message Converter
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueues(
                disbursementSuccessQueue(),
                disbursementFailQueue(),
                loanApprovedSuccessQueue(),
                loanApprovedFailQueue(),
                loanRemindQueue(),
                loanCompletionQueue(),
                loanCreateAccountQueue()
        );
        container.setObservationEnabled(true);
        return container;
    }
}
