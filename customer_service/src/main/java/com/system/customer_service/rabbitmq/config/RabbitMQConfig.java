package com.system.customer_service.rabbitmq.config;

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

    @Value("${rabbitmq.queue.customer.welcome}")
    private String queue;

    @Value("${rabbitmq.queue.customer.update}")
    private String queue1;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key.welcome}")
    private String routingKey;

    @Value("${rabbitmq.routing.key.update}")
    private String routingKey1;

    @Bean
    public Queue queue() {

        return new Queue(queue);
    }

    @Bean
    public Queue queue1() {

        return new Queue(queue1);
    }

    @Bean
    public TopicExchange exchange() {

        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding() {

        return BindingBuilder.bind(queue())
                .to(exchange())
                .with(routingKey);
    }

    @Bean
    public Binding binding1() {

        return BindingBuilder.bind(queue1())
                .to(exchange())
                .with(routingKey1);
    }

    @Bean
    public MessageConverter converter() {

        return new Jackson2JsonMessageConverter();
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setObservationEnabled(true);
        return container;
    }

    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
