package com.example.stadiumservice.service;

import com.example.stadiumservice.config.RabbitMQConfig;
import com.example.stadiumservice.dto.BattleMessage;
import com.example.stadiumservice.dto.PokemonDTO;
import com.example.stadiumservice.dto.UserDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
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
            System.out.println("Batalha não encontrada: " + battleId);
            return;
        }

        if (battle.isBattleEnded()) {
            System.out.println("Batalha já terminou: " + battleId);
            return;
        }

        System.out.println("Ação recebida de " + message.getUser().getName() + ": " + message.getAction());

        if (message.getAction() == BattleMessage.BattleAction.FLEE) {
            handlePlayerFlee(battle, playerId);
            return;
        }

        if (message.getAction() == BattleMessage.BattleAction.SWITCH_POKEMON && message.getTarget() != null) {
            boolean switchSuccess = handlePokemonSwitch(battle, playerId, message.getTarget());
            if (switchSuccess) {
                battle.addPendingAction(playerId, message.getAction());
            } else {
                sendSwitchFailedResponse(battle, playerId);
                return;
            }
        } else {
            battle.addPendingAction(playerId, message.getAction());
        }

        if (battle.bothPlayersActed()) {
            processTurn(battle);
        } else {
            sendWaitingForOpponentResponse(battle, playerId);
        }
    }

    private boolean handlePokemonSwitch(BattleSession battle, String playerId, int newIndex) {
        boolean isPlayer1 = String.valueOf(battle.getPlayer1().getId()).equals(playerId);

        boolean success = isPlayer1 ?
                battle.switchPokemon1(newIndex) :
                battle.switchPokemon2(newIndex);

        if (success) {
            String pokemonName = isPlayer1 ?
                    battle.getCurrentPokemon1().getName() :
                    battle.getCurrentPokemon2().getName();
            System.out.println(battle.getPlayer(playerId).getName() + " trocou para " + pokemonName);
        }

        return success;
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
        System.out.println("Processando turno: " + battle.getBattleId());

        System.out.println("DEBUG - Pokemon ativo Player 1: " + battle.getCurrentPokemon1().getName());
        System.out.println("DEBUG - Pokemon ativo Player 2: " + battle.getCurrentPokemon2().getName());

        System.out.println("HP Antes - " + battle.getCurrentPokemon1().getName() + ": " +
                battle.getCurrentPokemon1().getCurrentHp() + "/" + battle.getCurrentPokemon1().getHp());
        System.out.println("HP Antes - " + battle.getCurrentPokemon2().getName() + ": " +
                battle.getCurrentPokemon2().getCurrentHp() + "/" + battle.getCurrentPokemon2().getHp());

        BattleMessage.BattleAction action1 = battle.getPendingActions().get(String.valueOf(battle.getPlayer1().getId()));
        BattleMessage.BattleAction action2 = battle.getPendingActions().get(String.valueOf(battle.getPlayer2().getId()));

        if (battle.getCurrentTurn().equals("player1")) {
            processPlayerAction(battle, action1, true);
            if (!battle.getCurrentPokemon2().isFainted()) {
                processPlayerAction(battle, action2, false);
            }
        } else {
            processPlayerAction(battle, action2, false);
            if (!battle.getCurrentPokemon1().isFainted()) {
                processPlayerAction(battle, action1, true);
            }
        }

        checkAutoSwitch(battle);

        checkBattleEnd(battle);

        battle.clearPendingActions();
        battle.switchTurn();

        if (!battle.isBattleEnded()) {
            sendPlayerActionRequest(battle, battle.getCurrentTurn().equals("player1") ?
                    String.valueOf(battle.getPlayer1().getId()) : String.valueOf(battle.getPlayer2().getId()));
        }

        System.out.println("HP Depois - " + battle.getCurrentPokemon1().getName() + ": " +
                battle.getCurrentPokemon1().getCurrentHp() + "/" + battle.getCurrentPokemon1().getHp());
        System.out.println("HP Depois - " + battle.getCurrentPokemon2().getName() + ": " +
                battle.getCurrentPokemon2().getCurrentHp() + "/" + battle.getCurrentPokemon2().getHp());
    }

    private void processPlayerAction(BattleSession battle, BattleMessage.BattleAction action, boolean isPlayer1) {
        PokemonDTO attacker = isPlayer1 ? battle.getCurrentPokemon1() : battle.getCurrentPokemon2();
        PokemonDTO defender = isPlayer1 ? battle.getCurrentPokemon2() : battle.getCurrentPokemon1();
        UserDTO player = isPlayer1 ? battle.getPlayer1() : battle.getPlayer2();

        BattleEngine.BattleResult result = new BattleEngine.BattleResult(0, player.getName() + " agiu.");

        if (action == BattleMessage.BattleAction.ATTACK) {
            result = battleEngine.calculateBattle(attacker, defender);
            defender.setCurrentHp(defender.getCurrentHp() - result.getDamage());
            System.out.println(player.getName() + " atacou! " + result.getLog());

        } else if (action == BattleMessage.BattleAction.SWITCH_POKEMON) {
            result = new BattleEngine.BattleResult(0, player.getName() + " trocou para " + attacker.getName() + "!");
            System.out.println(result.getLog());
        }

        sendIndividualTurnResult(battle, String.valueOf(player.getId()), result.getDamage(), result.getLog());
    }

    private void checkAutoSwitch(BattleSession battle) {
        if (battle.getCurrentPokemon1().isFainted()) {
            System.out.println(battle.getCurrentPokemon1().getName() + " desmaiou! Procurando substituto...");
            boolean switched = autoSwitchPokemon(battle, battle.getPlayer1(), true);
            if (!switched) {
                System.out.println(battle.getPlayer1().getName() + " não tem mais Pokemon!");
            }
        }

        if (battle.getCurrentPokemon2().isFainted()) {
            System.out.println(battle.getCurrentPokemon2().getName() + " desmaiou! Procurando substituto...");
            boolean switched = autoSwitchPokemon(battle, battle.getPlayer2(), false);
            if (!switched) {
                System.out.println(battle.getPlayer2().getName() + " não tem mais Pokemon!");
            }
        }
    }

    private void checkBattleEnd(BattleSession battle) {
        boolean pokemon1Fainted = battle.getCurrentPokemon1().isFainted();
        boolean pokemon2Fainted = battle.getCurrentPokemon2().isFainted();

        System.out.println("Verificando fim de batalha:");
        System.out.println(battle.getCurrentPokemon1().getName() + " desmaiou: " + pokemon1Fainted);
        System.out.println(battle.getCurrentPokemon2().getName() + " desmaiou: " + pokemon2Fainted);

        if (pokemon1Fainted || pokemon2Fainted) {
            System.out.println("Pokemon desmaiou! Verificando time inteiro...");

            boolean team1HasAlive = battle.getPlayer1().getTeam().stream().anyMatch(p -> !p.isFainted());
            boolean team2HasAlive = battle.getPlayer2().getTeam().stream().anyMatch(p -> !p.isFainted());

            System.out.println("Time " + battle.getPlayer1().getName() + " tem Pokemon vivo: " + team1HasAlive);
            System.out.println("Time " + battle.getPlayer2().getName() + " tem Pokemon vivo: " + team2HasAlive);

            if (!team1HasAlive || !team2HasAlive) {
                battle.setBattleEnded(true);

                String winnerId = !team1HasAlive ? String.valueOf(battle.getPlayer2().getId()) :
                        String.valueOf(battle.getPlayer1().getId());
                String winnerName = !team1HasAlive ? battle.getPlayer2().getName() :
                        battle.getPlayer1().getName();

                System.out.println("Batalha " + battle.getBattleId() + " terminou! Vencedor: " + winnerName);
                sendBattleEndResponse(battle, winnerId, winnerName);
                activeBattles.remove(battle.getBattleId());
            } else {
                System.out.println("Time ainda tem Pokemon vivo, continuando batalha...");
            }
        }
    }

    private void sendSwitchFailedResponse(BattleSession battle, String playerId) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.PLAYER_ACTION);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setBattleLog("ERRO: Nao foi possivel trocar de Pokemon! Tente outro.");

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("Troca falhou para: " + player.getName());
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

    private boolean autoSwitchPokemon(BattleSession battle, UserDTO player, boolean isPlayer1) {
        List<PokemonDTO> team = player.getTeam();
        for (int i = 0; i < team.size(); i++) {
            if (!team.get(i).isFainted()) {
                if (isPlayer1) {
                    battle.switchPokemon1(i);
                } else {
                    battle.switchPokemon2(i);
                }
                System.out.println(player.getName() + " trocou automaticamente para " + team.get(i).getName());
                return true;
            }
        }
        return false;
    }

    private void handlePlayerFlee(BattleSession battle, String playerId) {
        System.out.println(battle.getPlayer(playerId).getName() + " fugiu da batalha!");

        boolean isPlayer1 = String.valueOf(battle.getPlayer1().getId()).equals(playerId);
        String winnerId = isPlayer1 ? String.valueOf(battle.getPlayer2().getId()) : String.valueOf(battle.getPlayer1().getId());
        String winnerName = isPlayer1 ? battle.getPlayer2().getName() : battle.getPlayer1().getName();
        String fleerName = isPlayer1 ? battle.getPlayer1().getName() : battle.getPlayer2().getName();

        battle.setBattleEnded(true);

        sendFleeResult(battle, winnerId, winnerName, fleerName);

        activeBattles.remove(battle.getBattleId());
    }

    private void sendFleeResult(BattleSession battle, String winnerId, String winnerName, String fleerName) {
        sendIndividualFleeResult(battle, fleerName.equals(battle.getPlayer1().getName()) ?
                        String.valueOf(battle.getPlayer1().getId()) : String.valueOf(battle.getPlayer2().getId()),
                winnerName, true);

        sendIndividualFleeResult(battle, winnerId, fleerName, false);
    }

    private void sendIndividualFleeResult(BattleSession battle, String playerId, String otherPlayerName, boolean isFleer) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.BATTLE_END);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());

        if (isFleer) {
            message.setFrom(String.valueOf(battle.getPlayer(playerId).getId()));
            message.setBattleLog("Voce fugiu da batalha! " + otherPlayerName + " venceu!");
        } else {
            message.setFrom(playerId);
            message.setBattleLog(otherPlayerName + " fugiu da batalha! Voce venceu!");
        }

        message.setOpponentName(otherPlayerName);

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("Resultado de fuga enviado para: " + player.getName());
    }
}