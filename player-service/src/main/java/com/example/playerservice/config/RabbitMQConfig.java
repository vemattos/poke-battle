package com.example.playerservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BATTLE_REQUEST_QUEUE = "battle.request.queue";
    public static final String BATTLE_RESPONSE_QUEUE = "battle.response.queue";

    @Bean
    public Queue battleRequestQueue() {
        return new Queue(BATTLE_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue battleResponseQueue() {
        return new Queue(BATTLE_RESPONSE_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}