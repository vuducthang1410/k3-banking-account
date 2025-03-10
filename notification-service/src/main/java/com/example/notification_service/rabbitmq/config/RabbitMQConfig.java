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
    private String queue;

    @Bean
    public Queue queue() {

        return new Queue(queue);
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
