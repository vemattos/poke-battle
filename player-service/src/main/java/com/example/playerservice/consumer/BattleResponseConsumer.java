package com.example.playerservice.consumer;

import com.example.playerservice.controller.BattleController;
import com.example.playerservice.dto.BattleMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BattleResponseConsumer {

    private final BattleController battleController;

    public BattleResponseConsumer(BattleController battleController) {
        this.battleController = battleController;
    }

    @RabbitListener(queues = "battle.response.queue")
    public void receiveBattleResponse(BattleMessage message) {
        System.out.println("[PLAYER] Response: " + message.getType() + " - " +
                (message.getUser() != null ? message.getUser().getName() : "No User"));

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
        // ✅ CORREÇÃO: Registrar a instância da batalha
        if (message.getInstanceId() != null && message.getBattleId() != null) {
            battleController.registerBattleInstance(message.getBattleId(), message.getInstanceId());
        }

        System.out.println("   BATALHA INICIADA!");
        System.out.println("   ID: " + message.getBattleId());
        System.out.println("   Você: " + message.getUser().getName());
        System.out.println("   Oponente: " + message.getOpponentName());
        System.out.println("   Instância: " + message.getInstanceId());
        System.out.println("   Comando: POST /battle/" + message.getUser().getId() + "/attack?battleId=" + message.getBattleId());
    }

    private void handlePlayerAction(BattleMessage message) {
        System.out.println("SUA VEZ!");
        System.out.println("Batalha: " + message.getBattleId());
        System.out.println("Instância: " + message.getInstanceId());

        if (message.getBattleLog() != null &&
                (message.getBattleLog().contains("ERRO") ||
                        message.getBattleLog().contains("Nao foi possivel") ||
                        message.getBattleLog().contains("falhou"))) {
            System.out.println(message.getBattleLog());
        }

        System.out.println("Comandos:");
        System.out.println("- Ataque: POST /battle/" + message.getUser().getId() + "/attack?battleId=" + message.getBattleId());
        System.out.println("- Trocar: POST /battle/" + message.getUser().getId() + "/switch?battleId=" + message.getBattleId() + "&pokemonIndex=0");
        System.out.println("- Fugir: POST /battle/" + message.getUser().getId() + "/flee?battleId=" + message.getBattleId());
        System.out.println("- Ver time: GET /battle/" + message.getUser().getId() + "/team");
    }

    private void handleTurnResult(BattleMessage message) {
        System.out.println("RESULTADO DO TURNO:");
        if (message.getBattleLog() != null) {
            System.out.println("   " + message.getBattleLog());
        }
        if (message.getDamage() != null) {
            System.out.println("   Dano causado: " + message.getDamage());
        }
        System.out.println("   Instância: " + message.getInstanceId());
    }

    private void handleBattleEnd(BattleMessage message) {
        System.out.println("BATALHA TERMINOU!");

        if (message.getBattleLog() != null) {
            System.out.println(message.getBattleLog());
        } else {
            System.out.println("Vencedor: " + (message.getOpponentName() != null ? message.getOpponentName() : "User " + message.getFrom()));
        }

        System.out.println("ID: " + message.getBattleId());
        System.out.println("Instância: " + message.getInstanceId());
        System.out.println("A batalha terminou. Não envie mais ações.");
    }
}