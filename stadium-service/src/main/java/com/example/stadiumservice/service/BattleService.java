package com.example.stadiumservice.service;

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

    private final ConcurrentHashMap<String, BattleSession> activeBattles = new ConcurrentHashMap<>();

    public BattleService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void handlePlayerLogin(UserDTO user) {
        System.out.println("Jogador " + user.getName() + " entrou no stadium");

        if (waitingPlayers.isEmpty()) {
            waitingPlayers.offer(user);
            System.out.println(user.getName() + " esperando oponente...");

            sendWaitingMessage(user);
        } else {
            UserDTO player1 = waitingPlayers.poll();
            UserDTO player2 = user;

            System.out.println("Batalha encontrada: " + player1.getName() + " vs " + player2.getName());
            startBattle(player1, player2);
        }
    }

    private void startBattle(UserDTO player1, UserDTO player2) {
        String battleId = "battle-" + System.currentTimeMillis();

        BattleSession battle = new BattleSession(battleId, player1, player2);
        activeBattles.put(battleId, battle);

        sendBattleStartMessage(player1, battleId);
        sendBattleStartMessage(player2, battleId);

        System.out.println("Batalha " + battleId + " iniciada!");
    }

    private void sendWaitingMessage(UserDTO user) {
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.LOGIN);
        message.setUser(user);
        message.setBattleId("waiting");

        rabbitTemplate.convertAndSend("battle.exchange", "player." + user.getId(), message);
    }

    private void sendBattleStartMessage(UserDTO user, String battleId) {
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.BATTLE_START);
        message.setUser(user);
        message.setBattleId(battleId);

        rabbitTemplate.convertAndSend("battle.exchange", "player." + user.getId(), message);
    }

    public void handleBattleAction(BattleMessage message) {
        System.out.println("Ação de batalha recebida: " + message);
    }

    private static class BattleSession {
        private final String battleId;
        private final UserDTO player1;
        private final UserDTO player2;

        public BattleSession(String battleId, UserDTO player1, UserDTO player2) {
            this.battleId = battleId;
            this.player1 = player1;
            this.player2 = player2;
        }

        public String getBattleId() { return battleId; }
        public UserDTO getPlayer1() { return player1; }
        public UserDTO getPlayer2() { return player2; }
    }
}