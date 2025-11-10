package com.example.playerservice.consumer;

import com.example.playerservice.dto.BattleMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BattleResponseConsumer {

    @RabbitListener(queues = "battle.response.queue")
    public void receiveBattleResponse(BattleMessage message) {
        System.out.println("[PLAYER] Response: " + message.getType() + " - " + message.getUser().getName());

        switch (message.getType()) {
            case BATTLE_START:
                handleBattleStart(message);
                break;

            case PLAYER_ACTION:
                handlePlayerAction(message);
                break;

            case TURN_RESULT:
                handleTurnResult(message);
                break;

            case BATTLE_END:
                handleBattleEnd(message);
                break;

            case LOGIN:
                System.out.println("Aguardando oponente...");
                break;
        }
    }

    private void handleBattleStart(BattleMessage message) {
        System.out.println("   BATALHA INICIADA!");
        System.out.println("   ID: " + message.getBattleId());
        System.out.println("   Você: " + message.getUser().getName());
        System.out.println("   Oponente: " + message.getOpponentName());
        System.out.println("   Comando: POST /battle/" + message.getUser().getId() + "/attack?battleId=" + message.getBattleId());
    }

    private void handlePlayerAction(BattleMessage message) {
        System.out.println("    SUA VEZ!");
        System.out.println("   Batalha: " + message.getBattleId());
        System.out.println("   Envie: POST /battle/" + message.getUser().getId() + "/attack?battleId=" + message.getBattleId());
    }

    private void handleTurnResult(BattleMessage message) {
        System.out.println("RESULTADO DO TURNO:");
        if (message.getBattleLog() != null) {
            System.out.println("   " + message.getBattleLog());
        }
        if (message.getDamage() != null) {
            System.out.println("   Dano causado: " + message.getDamage());
        }
    }

    private void handleBattleEnd(BattleMessage message) {
        System.out.println("   BATALHA TERMINOU!");
        System.out.println("   Vencedor: " + (message.getOpponentName() != null ? message.getOpponentName() : "User " + message.getFrom()));
        System.out.println("   ID: " + message.getBattleId());
    }
}