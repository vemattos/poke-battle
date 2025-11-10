package com.example.stadiumservice.service;

import com.example.stadiumservice.dto.BattleMessage;
import com.example.stadiumservice.dto.PokemonDTO;
import com.example.stadiumservice.dto.UserDTO;
import lombok.Data;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BattleSession {
    private final String battleId;
    private final UserDTO player1;
    private final UserDTO player2;
    private PokemonDTO currentPokemon1;
    private PokemonDTO currentPokemon2;
    private String currentTurn;
    private final ConcurrentHashMap<String, BattleMessage.BattleAction> pendingActions = new ConcurrentHashMap<>();
    private boolean battleEnded = false;

    public BattleSession(String battleId, UserDTO player1, UserDTO player2) {
        this.battleId = battleId;
        this.player1 = player1;
        this.player2 = player2;
        this.currentPokemon1 = player1.getTeam().get(0);
        this.currentPokemon2 = player2.getTeam().get(0);
        this.currentTurn = "player1";
    }

    public UserDTO getPlayer(String playerId) {
        if (String.valueOf(player1.getId()).equals(playerId)) return player1;
        if (String.valueOf(player2.getId()).equals(playerId)) return player2;
        return null;
    }

    public String getOpponentId(String playerId) {
        if (String.valueOf(player1.getId()).equals(playerId)) return String.valueOf(player2.getId());
        if (String.valueOf(player2.getId()).equals(playerId)) return String.valueOf(player1.getId());
        return null;
    }

    public void addPendingAction(String playerId, BattleMessage.BattleAction action) {
        pendingActions.put(playerId, action);
    }

    public boolean bothPlayersActed() {
        return pendingActions.containsKey(String.valueOf(player1.getId())) &&
                pendingActions.containsKey(String.valueOf(player2.getId()));
    }

    public void clearPendingActions() {
        pendingActions.clear();
    }

    public void switchTurn() {
        this.currentTurn = this.currentTurn.equals("player1") ? "player2" : "player1";
    }
}