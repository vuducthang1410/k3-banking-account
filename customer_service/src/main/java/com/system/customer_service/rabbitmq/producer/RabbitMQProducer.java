package com.system.customer_service.rabbitmq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQProducer {

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key.welcome}")
    private String routingKey;

    @Value("${rabbitmq.routing.key.update}")
    private String routingKey1;

    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {

        log.info("Message sent -> {}", message);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    public void sendMessage1(String message) {

        log.info("Message sent -> {}", message);
        rabbitTemplate.convertAndSend(exchange, routingKey1, message);
    }
}