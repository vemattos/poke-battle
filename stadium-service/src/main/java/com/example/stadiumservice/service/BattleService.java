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
    private final BattleEngine battleEngine;
    private final ConcurrentLinkedQueue<UserDTO> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, BattleSession> activeBattles = new ConcurrentHashMap<>();

    public BattleService(RabbitTemplate rabbitTemplate, BattleEngine battleEngine) {
        this.rabbitTemplate = rabbitTemplate;
        this.battleEngine = battleEngine;
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

    public void handleBattleAction(BattleMessage message) {
        String battleId = message.getBattleId();
        String playerId = String.valueOf(message.getUser().getId());

        BattleSession battle = activeBattles.get(battleId);
        if (battle == null) {
            System.out.println("Batalha não encontrada (já terminou?): " + battleId);
            return;
        }

        if (battle.isBattleEnded()) {
            System.out.println("Batalha já terminou: " + battleId);
            return;
        }

        System.out.println("Ação recebida de " + message.getUser().getName() + ": " + message.getAction());

        battle.addPendingAction(playerId, message.getAction());

        if (battle.bothPlayersActed()) {
            processTurn(battle);
        } else {
            sendWaitingForOpponentResponse(battle, playerId);
        }
    }

    private void startBattle(UserDTO player1, UserDTO player2) {
        String battleId = "battle-" + System.currentTimeMillis();

        BattleSession battle = new BattleSession(battleId, player1, player2);
        activeBattles.put(battleId, battle);

        System.out.println("Batalha " + battleId + " iniciada!");

        sendBattleStartResponse(player1, battleId, player2.getName());
        sendBattleStartResponse(player2, battleId, player1.getName());

        sendPlayerActionRequest(battle, String.valueOf(player1.getId()));
    }

    private void processTurn(BattleSession battle) {
        System.out.println("Processando turno da batalha: " + battle.getBattleId());

        BattleEngine.BattleResult result1 = battleEngine.calculateBattle(
                battle.getCurrentPokemon1(),
                battle.getCurrentPokemon2()
        );

        BattleEngine.BattleResult result2 = battleEngine.calculateBattle(
                battle.getCurrentPokemon2(),
                battle.getCurrentPokemon1()
        );

        battle.getCurrentPokemon2().setCurrentHp(
                battle.getCurrentPokemon2().getCurrentHp() - result1.getDamage()
        );

        battle.getCurrentPokemon1().setCurrentHp(
                battle.getCurrentPokemon1().getCurrentHp() - result2.getDamage()
        );

        sendTurnResult(battle, result1, result2);

        checkBattleEnd(battle);

        battle.clearPendingActions();
        battle.switchTurn();

        if (!battle.isBattleEnded()) {
            sendPlayerActionRequest(battle, battle.getCurrentTurn().equals("player1") ?
                    String.valueOf(battle.getPlayer1().getId()) : String.valueOf(battle.getPlayer2().getId()));
        }
    }

    private void checkBattleEnd(BattleSession battle) {
        boolean pokemon1Fainted = battle.getCurrentPokemon1().getCurrentHp() <= 0;
        boolean pokemon2Fainted = battle.getCurrentPokemon2().getCurrentHp() <= 0;

        if (pokemon1Fainted || pokemon2Fainted) {
            battle.setBattleEnded(true);

            String winnerId = pokemon1Fainted ? String.valueOf(battle.getPlayer2().getId()) :
                    String.valueOf(battle.getPlayer1().getId());
            String winnerName = pokemon1Fainted ? battle.getPlayer2().getName() :
                    battle.getPlayer1().getName();

            System.out.println("Batalha " + battle.getBattleId() + " terminou! Vencedor: " + winnerName);
            sendBattleEndResponse(battle, winnerId, winnerName);

            activeBattles.remove(battle.getBattleId());
        }
    }


    private void sendWaitingResponse(UserDTO user) {
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.LOGIN);
        message.setUser(user);
        message.setBattleId("waiting");

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
    }

    private void sendBattleStartResponse(UserDTO user, String battleId, String opponentName) {
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.BATTLE_START);
        message.setUser(user);
        message.setBattleId(battleId);
        message.setOpponentName(opponentName);

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
    }

    private void sendPlayerActionRequest(BattleSession battle, String playerId) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.PLAYER_ACTION);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("Solicitando ação de: " + player.getName());
    }

    private void sendWaitingForOpponentResponse(BattleSession battle, String playerId) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.PLAYER_ACTION);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("Aguardando oponente para: " + player.getName());
    }

    private void sendTurnResult(BattleSession battle, BattleEngine.BattleResult result1, BattleEngine.BattleResult result2) {
        sendIndividualTurnResult(battle, String.valueOf(battle.getPlayer1().getId()),
                result1.getDamage(), result1.getLog());

        sendIndividualTurnResult(battle, String.valueOf(battle.getPlayer2().getId()),
                result2.getDamage(), result2.getLog());
    }

    private void sendIndividualTurnResult(BattleSession battle, String playerId, int damage, String log) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.TURN_RESULT);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setDamage(damage);
        message.setBattleLog(log);

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("Resultado para " + player.getName() + ": " + log);
    }

    private void sendBattleEndResponse(BattleSession battle, String winnerId, String winnerName) {
        sendIndividualBattleEnd(battle, String.valueOf(battle.getPlayer1().getId()), winnerId, winnerName);
        sendIndividualBattleEnd(battle, String.valueOf(battle.getPlayer2().getId()), winnerId, winnerName);
    }

    private void sendIndividualBattleEnd(BattleSession battle, String playerId, String winnerId, String winnerName) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.BATTLE_END);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setFrom(winnerId);
        message.setOpponentName(winnerName);

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("Fim de batalha enviado para: " + player.getName() + " | Vencedor: " + winnerName);
    }
}