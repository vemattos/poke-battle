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
        String playerName = message.getUser() != null ? message.getUser().getName() : "Unknown";
        String playerId = message.getUser() != null ? String.valueOf(message.getUser().getId()) : "Unknown";
        
        System.out.println("[" + playerName + "] Recebeu: " + message.getType() +
                " | Battle: " + message.getBattleId() + " | ID: " + playerId);

        switch (message.getType()) {
            case BATTLE_START:
                handleBattleStart(message, playerName);
                break;

            case PLAYER_ACTION:
                handlePlayerAction(message, playerName);
                break;

            case TURN_RESULT:
                handleTurnResult(message, playerName);
                break;

            case BATTLE_END:
                handleBattleEnd(message, playerName);
                break;

            case LOGIN:
                System.out.println("[" + playerName + "] Aguardando oponente...");
                break;
        }
    }

    private void handleBattleStart(BattleMessage message, String playerName) {
        if (message.getInstanceId() != null && message.getBattleId() != null) {
            battleController.registerBattleInstance(message.getBattleId(), message.getInstanceId());
        }

        System.out.println("[" + playerName + "] BATALHA INICIADA!");
        System.out.println("   ID: " + message.getBattleId());
        System.out.println("   Você: " + playerName);
        System.out.println("   Oponente: " + message.getOpponentName());
        System.out.println("   Instância: " + message.getInstanceId());

        System.out.println("   Comandos disponíveis (quando for sua vez):");
        System.out.println("   Ataque: POST /battle/" + message.getUser().getId() +
                "/attack?battleId=" + message.getBattleId());
        System.out.println("   Trocar Pokémon: POST /battle/" + message.getUser().getId() +
                "/switch?battleId=" + message.getBattleId() + "&pokemonIndex=0");
        System.out.println("   Fugir: POST /battle/" + message.getUser().getId() +
                "/flee?battleId=" + message.getBattleId());
        System.out.println("   Ver time: GET /battle/" + message.getUser().getId() + "/team");
        System.out.println("");
    }

    private void handlePlayerAction(BattleMessage message, String playerName) {
        System.out.println("[" + playerName + "] SUA VEZ!");
        System.out.println("   Batalha: " + message.getBattleId());
        System.out.println("   Instância: " + message.getInstanceId());
        System.out.println("");

        System.out.println("   Escolha sua ação:");
        System.out.println("   ATACAR: POST /battle/" + message.getUser().getId() +
                "/attack?battleId=" + message.getBattleId());
        System.out.println("   TROCAR POKÉMON: POST /battle/" + message.getUser().getId() +
                "/switch?battleId=" + message.getBattleId() + "&pokemonIndex=0");
        System.out.println("   FUGIR: POST /battle/" + message.getUser().getId() +
                "/flee?battleId=" + message.getBattleId());
        System.out.println("");
        System.out.println("   Comandos adicionais:");
        System.out.println("   VER TIME: GET /battle/" + message.getUser().getId() + "/team");
        System.out.println("   STATUS: GET /battle/instances");
        System.out.println("");

        if (message.getBattleLog() != null &&
                (message.getBattleLog().contains("ERRO") ||
                        message.getBattleLog().contains("Nao foi possivel") ||
                        message.getBattleLog().contains("falhou") ||
                        message.getBattleLog().contains("AGUARDE"))) {
            System.out.println("   " + message.getBattleLog());
            System.out.println("");
        }
    }

    private void handleTurnResult(BattleMessage message, String playerName) {
        System.out.println("[" + playerName + "] RESULTADO DO TURNO:");
        if (message.getBattleLog() != null && !message.getBattleLog().isEmpty()) {
            System.out.println("   " + message.getBattleLog());
        }
        if (message.getDamage() != null) {
            System.out.println("   Dano: " + message.getDamage());
        }
        System.out.println("   Instância: " + message.getInstanceId());
        System.out.println("");
    }

    private void handleBattleEnd(BattleMessage message, String playerName) {
        System.out.println("[" + playerName + "] BATALHA TERMINOU!");
        System.out.println("   ID: " + message.getBattleId());
        System.out.println("   Instância: " + message.getInstanceId());

        if (message.getBattleLog() != null) {
            System.out.println("   " + message.getBattleLog());
        } else {
            System.out.println("   Vencedor: " +
                    (message.getOpponentName() != null ? message.getOpponentName() : "User " + message.getFrom()));
        }

        System.out.println("");
        System.out.println("   A batalha terminou. Não envie mais ações.");
        System.out.println("");
    }
}
