package com.example.playerservice.consumer;

import com.example.playerservice.dto.BattleMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BattleResponseConsumer {

    @RabbitListener(queues = "battle.response.queue")
    public void receiveBattleResponse(BattleMessage message) {
        System.out.println("[PLAYER] Response recebida: " + message.getType() + " - " + message.getUser().getName());

        switch (message.getType()) {
            case BATTLE_START:
                System.out.println("Batalha " + message.getBattleId() + " iniciou! Oponente: " + message.getOpponentName());
                break;
            case LOGIN:
                System.out.println("Aguardando oponente...");
                break;
            default:
                System.out.println("Mensagem recebida: " + message.getType());
        }
    }
}