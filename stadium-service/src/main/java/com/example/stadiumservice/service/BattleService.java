package com.example.stadiumservice.service;

import com.example.stadiumservice.config.RabbitMQConfig;
import com.example.stadiumservice.dto.BattleMessage;
import com.example.stadiumservice.dto.UserDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class BattleService {

    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentLinkedQueue<UserDTO> waitingPlayers = new ConcurrentLinkedQueue<>();

    public BattleService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void handlePlayerLogin(UserDTO user) {
        System.out.println("Jogador " + user.getName() + " entrou no stadium");

        if (waitingPlayers.isEmpty()) {
            waitingPlayers.offer(user);
            System.out.println(user.getName() + " esperando oponente...");
            sendWaitingResponse(user);
        } else {
            UserDTO player1 = waitingPlayers.poll();
            UserDTO player2 = user;

            System.out.println("Batalha encontrada: " + player1.getName() + " vs " + player2.getName());
            startBattle(player1, player2);
        }
    }

    private void startBattle(UserDTO player1, UserDTO player2) {
        String battleId = "battle-" + System.currentTimeMillis();

        System.out.println("Batalha " + battleId + " iniciada!");

        sendBattleStartResponse(player1, battleId, player2.getName());
        sendBattleStartResponse(player2, battleId, player1.getName());
    }

    private void sendWaitingResponse(UserDTO user) {
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.LOGIN);
        message.setUser(user);
        message.setBattleId("waiting");

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("[STADIUM] Waiting response enviada para: " + user.getName());
    }

    private void sendBattleStartResponse(UserDTO user, String battleId, String opponentName) {
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.BATTLE_START);
        message.setUser(user);
        message.setBattleId(battleId);
        message.setOpponentName(opponentName);

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("[STADIUM] Battle start enviado para: " + user.getName() + " vs " + opponentName);
    }

    public void handleBattleAction(BattleMessage message) {
        System.out.println("🎯 Ação de batalha recebida: " + message.getBattleId());
        // Implementar lógica de batalha
    }
}