package com.example.stadiumservice.consumer;

import com.example.stadiumservice.config.RabbitMQConfig;
import com.example.stadiumservice.dto.BattleMessage;
import com.example.stadiumservice.service.BattleService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BattleConsumer {

    private final BattleService battleService;

    public BattleConsumer(BattleService battleService) {
        this.battleService = battleService;
    }

    @RabbitListener(queues = RabbitMQConfig.BATTLE_REQUEST_QUEUE)
    public void receiveBattleRequest(BattleMessage message) {
        System.out.println("[STADIUM] Request recebido: " + message.getType() + " - " + message.getUser().getName());

        switch (message.getType()) {
            case LOGIN:
                battleService.handlePlayerLogin(message.getUser());
                break;
            case PLAYER_ACTION:
                battleService.handleBattleAction(message);
                break;
            default:
                System.out.println("Tipo de request não reconhecido: " + message.getType());
        }
    }
}