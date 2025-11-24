package com.example.stadiumservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElectionQueueConfig {

    private final RabbitMQConfig rabbitMQConfig;
    private final RabbitAdmin rabbitAdmin;

    public ElectionQueueConfig(RabbitMQConfig rabbitMQConfig, RabbitAdmin rabbitAdmin) {
        this.rabbitMQConfig = rabbitMQConfig;
        this.rabbitAdmin = rabbitAdmin;
    }

    @Bean
    public Queue electionQueue() {
        String instanceId = "hoenn";
        String queueName = rabbitMQConfig.getElectionQueueName(instanceId);

        Queue queue = rabbitMQConfig.createElectionQueue(instanceId);

        rabbitAdmin.declareQueue(queue);
        System.out.println("Fila de eleição criada: " + queueName);

        return queue;
    }
}