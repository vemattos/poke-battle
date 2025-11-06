package com.example.stadiumservice.consumer;

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

    @RabbitListener(queues = "${rabbitmq.queue.name:battle.queue}")
    public void receiveMessage(BattleMessage message) {
        System.out.println("📨 Mensagem recebida no Stadium: " + message.getType() + " - " + message.getUser().getName());

        switch (message.getType()) {
            case LOGIN:
                battleService.handlePlayerLogin(message.getUser());
                break;
            case BATTLE_ACTION:
                battleService.handleBattleAction(message);
                break;
            default:
                System.out.println("Tipo de mensagem não reconhecido: " + message.getType());
        }
    }
}