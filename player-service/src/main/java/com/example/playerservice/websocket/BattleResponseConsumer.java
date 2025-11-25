package com.example.playerservice.websocket;

import com.example.playerservice.controller.BattleController;
import com.example.playerservice.dto.BattleMessage;
import com.example.playerservice.model.User;
import com.example.playerservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component("battleResponseConsumerWs")
public class BattleResponseConsumer {

    private final BattleController battleController;
    private final PlayerWebSocketHandler wsHandler;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BattleResponseConsumer(BattleController battleController,
                                  PlayerWebSocketHandler wsHandler, UserService userService) {
        this.battleController = battleController;
        this.wsHandler = wsHandler;
        this.userService = userService;
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
            default:
                break;
        }
    }

    private void forwardWs(BattleMessage message, int targetUserId) {
        try {
            String json = objectMapper.writeValueAsString(message);
            wsHandler.sendToPlayer(targetUserId, json);
        } catch (Exception e) {
            System.out.println("[PLAYER] Erro ao serializar/parar WS: " + e.getMessage());
        }
    }

    private void handleBattleStart(BattleMessage message) {
        // ✅ LOG PARA VERIFICAR SE opponentName CHEGOU
        System.out.println("🎯 DEBUG Consumer - OpponentName recebido: " + message.getOpponentName());
        System.out.println("🎯 DEBUG Consumer - User recebido: " +
                (message.getUser() != null ? message.getUser().getName() : "null"));

        // Registra instância
        if (message.getInstanceId() != null && message.getBattleId() != null) {
            battleController.registerBattleInstance(message.getBattleId(), message.getInstanceId());
        }

        System.out.println("   BATALHA INICIADA!");
        System.out.println("   ID: " + message.getBattleId());
        System.out.println("   Você: " + (message.getUser() != null ? message.getUser().getName() : "anonymous"));
        System.out.println("   Oponente: " + message.getOpponentName()); // ⚠️ Isso mostra nulo?
        System.out.println("   Instância: " + message.getInstanceId());
    }

    private void handlePlayerAction(BattleMessage message) {
        System.out.println("SUA VEZ!");
        if (message.getUser() != null) {
            forwardWs(message, message.getUser().getId());
        }
    }

    private void handleTurnResult(BattleMessage message) {
        System.out.println("RESULTADO DO TURNO:");
        if (message.getUser() != null) {
            forwardWs(message, message.getUser().getId());
        }
    }

    private void handleBattleEnd(BattleMessage message) {
        System.out.println("BATALHA TERMINOU!");
        if (message.getUser() != null) {
            forwardWs(message, message.getUser().getId());
        }
    }
}
