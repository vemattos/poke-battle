package com.example.stadiumservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String BATTLE_REQUEST_QUEUE_PREFIX = "battle.request.queue.stadium-";
    public static final String BATTLE_RESPONSE_QUEUE = "battle.response.queue";
    public static final String BATTLE_REQUEST_EXCHANGE = "battle.request.exchange";

    @Bean
    public FanoutExchange battleRequestExchange() {
        return new FanoutExchange(BATTLE_REQUEST_EXCHANGE, true, false);
    }

    @Bean
    public Queue battleResponseQueue() {
        return new Queue(BATTLE_RESPONSE_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    public Queue createBattleRequestQueue(String instanceId) {
        String queueName = BATTLE_REQUEST_QUEUE_PREFIX + instanceId;
        return new Queue(queueName, true, false, true); // auto-delete=true
    }

    public String getBattleRequestQueueName(String instanceId) {
        return BATTLE_REQUEST_QUEUE_PREFIX + instanceId;
    }
}