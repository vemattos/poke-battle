package com.example.stadiumservice.consumer;

import com.example.stadiumservice.config.RabbitMQConfig;
import com.example.stadiumservice.dto.ElectionMessage;
import com.example.stadiumservice.service.ElectionService;
import com.example.stadiumservice.service.StadiumService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ElectionConsumer {

    private final ElectionService electionService;
    private final StadiumService stadiumService;
    private final RabbitMQConfig rabbitMQConfig;

    public ElectionConsumer(ElectionService electionService,
                            StadiumService stadiumService,
                            RabbitMQConfig rabbitMQConfig) {
        this.electionService = electionService;
        this.stadiumService = stadiumService;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    @RabbitListener(queues = "#{@electionQueue.name}")
    public void receiveElectionMessage(ElectionMessage message) {
        electionService.processElectionMessage(message);
    }
}