package com.example.stadiumservice.service;

import com.example.stadiumservice.config.RabbitMQConfig;
import com.example.stadiumservice.dto.BattleMessage;
import com.example.stadiumservice.dto.PokemonDTO;
import com.example.stadiumservice.dto.PokemonRegion;
import com.example.stadiumservice.dto.UserDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class BattleService {

    private final RabbitTemplate rabbitTemplate;
    private final BattleEngine battleEngine;
    private final String instanceId;
    private final PokemonRegion region;
    private final ConcurrentLinkedQueue<UserDTO> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, BattleSession> activeBattles = new ConcurrentHashMap<>();

    public BattleService(RabbitTemplate rabbitTemplate, BattleEngine battleEngine,
                         String instanceId, PokemonRegion region) {
        this.rabbitTemplate = rabbitTemplate;
        this.battleEngine = battleEngine;
        this.instanceId = instanceId;
        this.region = region;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public PokemonRegion getRegion() {
        return region;
    }

    public String getRegionName() {
        return region.getName();
    }

    public List<UserDTO> getWaitingPlayers() {
        return new ArrayList<>(waitingPlayers);
    }

    public int getWaitingPlayersCount() {
        return waitingPlayers.size();
    }

    public int getActiveBattlesCount() {
        return activeBattles.size();
    }

    public List<com.example.stadiumservice.dto.BattleInfo> getActiveBattles() {
        return activeBattles.entrySet().stream()
                .map(entry -> {
                    BattleSession battle = entry.getValue();
                    return new com.example.stadiumservice.dto.BattleInfo(
                            entry.getKey(),
                            battle.getPlayer1(),
                            battle.getPlayer2(),
                            "active"
                    );
                })
                .collect(Collectors.toList());
    }

    public void handlePlayerLogin(UserDTO user) {
        System.out.println("Jogador " + user.getName() + " entrou na região " + region.getName());

        if (waitingPlayers.isEmpty()) {
            waitingPlayers.offer(user);
            System.out.println(user.getName() + " esperando oponente em " + region.getName() + "...");
            sendWaitingResponse(user);
        } else {
            UserDTO player1 = waitingPlayers.poll();
            UserDTO player2 = user;

            System.out.println("Batalha encontrada em " + region.getName() + ": " +
                    player1.getName() + " vs " + player2.getName());
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

        System.out.println("Ação recebida de " + message.getUser().getName() +
                " na região " + region.getName() + ": " + message.getAction());

        if (!battle.isPlayerTurn(playerId)) {
            System.out.println("Não é a vez de " + message.getUser().getName() + ". Aguarde seu turno.");
            sendNotYourTurnResponse(battle, playerId);
            return;
        }

        if (message.getAction() == BattleMessage.BattleAction.SWITCH_POKEMON && message.getTarget() != null) {
            boolean switchSuccess = handlePokemonSwitch(battle, playerId, message.getTarget());
            if (!switchSuccess) {
                sendSwitchFailedResponse(battle, playerId);
                return;
            }
        }

        boolean actionAdded = battle.addAction(playerId, message.getAction());
        if (!actionAdded) {
            System.out.println("Erro: Não foi possível adicionar ação para " + message.getUser().getName());
            return;
        }

        System.out.println("Ação registrada para " + message.getUser().getName() + ". Processando turno...");

        processTurn(battle);
    }

    private void startBattle(UserDTO player1, UserDTO player2) {
        String battleId = "battle-" + System.currentTimeMillis() + "-" + instanceId;

        BattleSession battle = new BattleSession(battleId, player1, player2);
        activeBattles.put(battleId, battle);

        System.out.println("Batalha " + battleId + " iniciada na região " + region.getName() + "!");

        sendBattleStartResponse(player1, battleId, player2.getName());
        sendBattleStartResponse(player2, battleId, player1.getName());

        sendPlayerActionRequest(battle, String.valueOf(player1.getId()));
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
            System.out.println("" + battle.getPlayer(playerId).getName() + " trocou para " + pokemonName + " em " + region.getName());
        }

        return success;
    }

    private void processTurn(BattleSession battle) {
        try {
            System.out.println("PROCESSANDO TURNO em " + region.getName() + ": " + battle.getBattleId());

            if (battle.getPendingAction() == null) {
                System.out.println("Nenhuma ação pendente para processar.");
                return;
            }

            executePlayerAction(battle);

            checkBattleEnd(battle);

            if (!battle.isBattleEnded()) {
                checkAutoSwitch(battle);

                checkBattleEnd(battle);

                if (!battle.isBattleEnded()) {
                    battle.nextTurn();

                    String nextPlayerId = battle.getCurrentPlayerId();
                    sendPlayerActionRequest(battle, nextPlayerId);

                    System.out.println("Vez de: " + battle.getPlayer(nextPlayerId).getName());
                }
            }

        } catch (Exception e) {
            System.out.println("ERRO CRITICO no processTurn: " + e.getMessage());
            e.printStackTrace();
            handleBattleError(battle, "Erro no processamento do turno: " + e.getMessage());
        }
    }

    private void executePlayerAction(BattleSession battle) {
        String playerId = battle.getPendingPlayerId();
        BattleMessage.BattleAction action = battle.getPendingAction();
        UserDTO player = battle.getPlayer(playerId);

        boolean isPlayer1 = String.valueOf(battle.getPlayer1().getId()).equals(playerId);
        PokemonDTO attacker = isPlayer1 ? battle.getCurrentPokemon1() : battle.getCurrentPokemon2();
        PokemonDTO defender = isPlayer1 ? battle.getCurrentPokemon2() : battle.getCurrentPokemon1();

        String battleLog = "";
        int damage = 0;

        if (action == BattleMessage.BattleAction.ATTACK) {
            BattleEngine.BattleResult result = battleEngine.calculateBattle(attacker, defender);
            defender.setCurrentHp(Math.max(0, defender.getCurrentHp() - result.getDamage()));
            damage = result.getDamage();
            battleLog = result.getLog();
            System.out.println(player.getName() + " atacou! " + battleLog);

        } else if (action == BattleMessage.BattleAction.SWITCH_POKEMON) {
            battleLog = player.getName() + " trocou para " + attacker.getName() + "!";
            System.out.println(battleLog);

        } else if (action == BattleMessage.BattleAction.FLEE) {
            battleLog = player.getName() + " fugiu da batalha!";
            System.out.println(battleLog);
            handlePlayerFlee(battle, playerId);
            return;
        }

        sendTurnResultToBoth(battle, battleLog, damage);

        System.out.println("STATUS APÓS AÇÃO:");
        System.out.println("   " + battle.getCurrentPokemon1().getName() + ": " +
                battle.getCurrentPokemon1().getCurrentHp() + "/" + battle.getCurrentPokemon1().getHp() + " HP");
        System.out.println("   " + battle.getCurrentPokemon2().getName() + ": " +
                battle.getCurrentPokemon2().getCurrentHp() + "/" + battle.getCurrentPokemon2().getHp() + " HP");
    }

    private void checkAutoSwitch(BattleSession battle) {
        if (battle.getCurrentPokemon1().isFainted()) {
            System.out.println(battle.getCurrentPokemon1().getName() + " desmaiou em " + region.getName() + "! Procurando substituto...");
            boolean switched = autoSwitchPokemon(battle, battle.getPlayer1(), true);
            if (switched) {
                sendAutoSwitchMessage(battle, battle.getPlayer1(), battle.getCurrentPokemon1());
            } else {
                System.out.println(battle.getPlayer1().getName() + " não tem mais Pokemon em " + region.getName() + "!");
            }
        }

        if (battle.getCurrentPokemon2().isFainted()) {
            System.out.println(battle.getCurrentPokemon2().getName() + " desmaiou em " + region.getName() + "! Procurando substituto...");
            boolean switched = autoSwitchPokemon(battle, battle.getPlayer2(), false);
            if (switched) {
                sendAutoSwitchMessage(battle, battle.getPlayer2(), battle.getCurrentPokemon2());
            } else {
                System.out.println(battle.getPlayer2().getName() + " não tem mais Pokemon em " + region.getName() + "!");
            }
        }
    }

    private void checkBattleEnd(BattleSession battle) {
        boolean pokemon1Fainted = battle.getCurrentPokemon1().isFainted();
        boolean pokemon2Fainted = battle.getCurrentPokemon2().isFainted();

        System.out.println("Verificando fim de batalha em " + region.getName() + ":");
        System.out.println(battle.getCurrentPokemon1().getName() + " desmaiou: " + pokemon1Fainted);
        System.out.println(battle.getCurrentPokemon2().getName() + " desmaiou: " + pokemon2Fainted);

        if (pokemon1Fainted || pokemon2Fainted) {
            System.out.println("Pokemon desmaiou em " + region.getName() + "! Verificando time inteiro...");

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

                System.out.println("Batalha " + battle.getBattleId() + " terminou em " + region.getName() + "! Vencedor: " + winnerName);
                sendBattleEndResponse(battle, winnerId, winnerName);
                activeBattles.remove(battle.getBattleId());
            }
        }
    }


    private void sendNotYourTurnResponse(BattleSession battle, String playerId) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.PLAYER_ACTION);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setInstanceId(instanceId);
        message.setBattleLog("AGUARDE: Não é sua vez de agir.");

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
    }

    private void sendTurnResultToBoth(BattleSession battle, String log, int damage) {
        sendIndividualTurnResult(battle, String.valueOf(battle.getPlayer1().getId()), damage, log);
        sendIndividualTurnResult(battle, String.valueOf(battle.getPlayer2().getId()), damage, log);
    }

    private void sendAutoSwitchMessage(BattleSession battle, UserDTO player, PokemonDTO newPokemon) {
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.TURN_RESULT);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setInstanceId(instanceId);
        message.setBattleLog(player.getName() + " trocou automaticamente para " + newPokemon.getName() + "!");

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
    }

    private void sendSwitchFailedResponse(BattleSession battle, String playerId) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.PLAYER_ACTION);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setInstanceId(instanceId);
        message.setBattleLog("ERRO: Nao foi possivel trocar de Pokemon! Tente outro.");

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("Troca falhou para: " + player.getName() + " em " + region.getName());
    }

    private void sendWaitingResponse(UserDTO user) {
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.LOGIN);
        message.setUser(user);
        message.setInstanceId(instanceId);
        message.setBattleId("waiting");

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
    }

    private void sendBattleStartResponse(UserDTO user, String battleId, String opponentName) {
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.BATTLE_START);
        message.setUser(user);
        message.setBattleId(battleId);
        message.setOpponentName(opponentName);
        message.setInstanceId(instanceId);

        System.out.println("Enviando BATTLE_START para: " + user.getName() +
                " (vs " + opponentName + ") | Battle: " + battleId);

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
    }

    private void sendPlayerActionRequest(BattleSession battle, String playerId) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.PLAYER_ACTION);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setInstanceId(instanceId);

        System.out.println("[BATTLE_SERVICE] Solicitando ação APENAS para: " + player.getName() +
                " | Battle: " + battle.getBattleId() +
                " | Turno atual: " + battle.getCurrentPlayerId());

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
    }

    private void sendIndividualTurnResult(BattleSession battle, String playerId, int damage, String log) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.TURN_RESULT);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setInstanceId(instanceId);
        message.setDamage(damage);
        message.setBattleLog(log);

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("Resultado para " + player.getName() + " em " + region.getName() + ": " + log);
    }

    private void sendBattleEndResponse(BattleSession battle, String winnerId, String winnerName) {
        sendIndividualBattleEnd(battle, String.valueOf(battle.getPlayer1().getId()), winnerId, winnerName);
        sendIndividualBattleEnd(battle, String.valueOf(battle.getPlayer2().getId()), winnerId, winnerName);
    }

    private void sendIndividualBattleEnd(BattleSession battle, String playerId, String winnerId, String winnerName) {
        UserDTO player = battle.getPlayer(playerId);
        boolean isWinner = String.valueOf(player.getId()).equals(winnerId);

        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.BATTLE_END);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setInstanceId(instanceId);
        message.setFrom(winnerId);
        message.setOpponentName(winnerName);

        if (isWinner) {
            message.setBattleLog("Parabéns! Você venceu a batalha!");
        } else {
            message.setBattleLog("Você perdeu! " + winnerName + " venceu a batalha.");
        }

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
                System.out.println(player.getName() + " trocou automaticamente para " + team.get(i).getName() + " em " + region.getName());
                return true;
            }
        }
        return false;
    }

    private void handlePlayerFlee(BattleSession battle, String playerId) {
        System.out.println(battle.getPlayer(playerId).getName() + " fugiu da batalha em " + region.getName() + "!");

        boolean isPlayer1 = String.valueOf(battle.getPlayer1().getId()).equals(playerId);
        String winnerId = isPlayer1 ? String.valueOf(battle.getPlayer2().getId()) : String.valueOf(battle.getPlayer1().getId());
        String winnerName = isPlayer1 ? battle.getPlayer2().getName() : battle.getPlayer1().getName();
        String fleerName = isPlayer1 ? battle.getPlayer1().getName() : battle.getPlayer2().getName();

        battle.setBattleEnded(true);
        sendFleeResult(battle, winnerId, winnerName, fleerName);
        activeBattles.remove(battle.getBattleId());
    }

    private void sendFleeResult(BattleSession battle, String winnerId, String winnerName, String fleerName) {
        sendIndividualFleeResult(battle,
                String.valueOf(battle.getPlayer1().getId()), winnerName,
                String.valueOf(battle.getPlayer1().getId()).equals(winnerId));
        sendIndividualFleeResult(battle,
                String.valueOf(battle.getPlayer2().getId()), winnerName,
                String.valueOf(battle.getPlayer2().getId()).equals(winnerId));
    }

    private void sendIndividualFleeResult(BattleSession battle, String playerId, String winnerName, boolean isWinner) {
        UserDTO player = battle.getPlayer(playerId);
        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.BATTLE_END);
        message.setUser(player);
        message.setBattleId(battle.getBattleId());
        message.setInstanceId(instanceId);

        if (isWinner) {
            message.setBattleLog("Seu oponente fugiu! Você venceu!");
        } else {
            message.setBattleLog("Você fugiu da batalha! " + winnerName + " venceu!");
        }

        message.setOpponentName(winnerName);

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, message);
        System.out.println("Resultado de fuga enviado para: " + player.getName());
    }

    private void handleBattleError(BattleSession battle, String errorMessage) {
        System.out.println("Lidando com erro na batalha em " + region.getName() + ": " + errorMessage);

        BattleMessage errorMsg = new BattleMessage();
        errorMsg.setType(BattleMessage.MessageType.BATTLE_END);
        errorMsg.setBattleId(battle.getBattleId());
        errorMsg.setInstanceId(instanceId);
        errorMsg.setBattleLog("ERRO: " + errorMessage + " | Batalha encerrada.");

        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_RESPONSE_QUEUE, errorMsg);
        activeBattles.remove(battle.getBattleId());
    }


    private static class BattleSession {
        private final String battleId;
        private final UserDTO player1;
        private final UserDTO player2;
        private int currentPokemonIndex1 = 0;
        private int currentPokemonIndex2 = 0;

        private String currentTurn = "player1";
        private BattleMessage.BattleAction pendingAction = null;
        private String pendingPlayerId = null;
        private boolean battleEnded = false;

        public BattleSession(String battleId, UserDTO player1, UserDTO player2) {
            this.battleId = battleId;
            this.player1 = player1;
            this.player2 = player2;

            player1.getTeam().forEach(p -> {
                if (p.getCurrentHp() <= 0) p.setCurrentHp(p.getHp());
            });
            player2.getTeam().forEach(p -> {
                if (p.getCurrentHp() <= 0) p.setCurrentHp(p.getHp());
            });
        }

        public PokemonDTO getCurrentPokemon1() {
            return player1.getTeam().get(currentPokemonIndex1);
        }

        public PokemonDTO getCurrentPokemon2() {
            return player2.getTeam().get(currentPokemonIndex2);
        }

        public boolean switchPokemon1(int newIndex) {
            if (newIndex >= 0 && newIndex < player1.getTeam().size()) {
                PokemonDTO newPokemon = player1.getTeam().get(newIndex);
                if (!newPokemon.isFainted() && newIndex != currentPokemonIndex1) {
                    currentPokemonIndex1 = newIndex;
                    return true;
                }
            }
            return false;
        }

        public boolean switchPokemon2(int newIndex) {
            if (newIndex >= 0 && newIndex < player2.getTeam().size()) {
                PokemonDTO newPokemon = player2.getTeam().get(newIndex);
                if (!newPokemon.isFainted() && newIndex != currentPokemonIndex2) {
                    currentPokemonIndex2 = newIndex;
                    return true;
                }
            }
            return false;
        }

        public UserDTO getPlayer(String playerId) {
            if (String.valueOf(player1.getId()).equals(playerId)) return player1;
            if (String.valueOf(player2.getId()).equals(playerId)) return player2;
            return null;
        }

        public boolean addAction(String playerId, BattleMessage.BattleAction action) {
            if (isPlayerTurn(playerId) && pendingAction == null) {
                pendingAction = action;
                pendingPlayerId = playerId;
                return true;
            }
            return false;
        }

        public boolean isPlayerTurn(String playerId) {
            if (currentTurn.equals("player1")) {
                return String.valueOf(player1.getId()).equals(playerId);
            } else {
                return String.valueOf(player2.getId()).equals(playerId);
            }
        }

        public void nextTurn() {
            this.currentTurn = this.currentTurn.equals("player1") ? "player2" : "player1";
            this.pendingAction = null;
            this.pendingPlayerId = null;
        }

        public String getCurrentPlayerId() {
            return currentTurn.equals("player1") ? String.valueOf(player1.getId()) : String.valueOf(player2.getId());
        }

        public String getBattleId() { return battleId; }
        public UserDTO getPlayer1() { return player1; }
        public UserDTO getPlayer2() { return player2; }
        public boolean isBattleEnded() { return battleEnded; }
        public void setBattleEnded(boolean battleEnded) { this.battleEnded = battleEnded; }
        public BattleMessage.BattleAction getPendingAction() { return pendingAction; }
        public String getPendingPlayerId() { return pendingPlayerId; }
    }
}