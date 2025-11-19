package com.example.stadiumservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BATTLE_REQUEST_QUEUE_1 = "battle.request.queue.stadium-1";
    public static final String BATTLE_REQUEST_QUEUE_2 = "battle.request.queue.stadium-2";
    public static final String BATTLE_REQUEST_QUEUE_3 = "battle.request.queue.stadium-3";
    public static final String BATTLE_RESPONSE_QUEUE = "battle.response.queue";

    @Bean
    public Queue battleRequestQueue1() {
        return new Queue(BATTLE_REQUEST_QUEUE_1, true);
    }

    @Bean
    public Queue battleRequestQueue2() {
        return new Queue(BATTLE_REQUEST_QUEUE_2, true);
    }

    @Bean
    public Queue battleRequestQueue3() {
        return new Queue(BATTLE_REQUEST_QUEUE_3, true);
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