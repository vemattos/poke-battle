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
    private int currentPokemonIndex1 = 0;
    private int currentPokemonIndex2 = 0;
    private String currentTurn = "player1";
    private final ConcurrentHashMap<String, BattleMessage.BattleAction> pendingActions = new ConcurrentHashMap<>();
    private boolean battleEnded = false;

    public BattleSession(String battleId, UserDTO player1, UserDTO player2) {
        this.battleId = battleId;
        this.player1 = player1;
        this.player2 = player2;
        player1.getTeam().forEach(pokemon -> {
            if (pokemon.getCurrentHp() <= 0) pokemon.setCurrentHp(pokemon.getHp());
        });
        player2.getTeam().forEach(pokemon -> {
            if (pokemon.getCurrentHp() <= 0) pokemon.setCurrentHp(pokemon.getHp());
        });
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

    public boolean isTeamFainted(UserDTO user) {
        return user.getTeam().stream().allMatch(PokemonDTO::isFainted);
    }
}