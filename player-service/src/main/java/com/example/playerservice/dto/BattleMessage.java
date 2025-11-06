package com.example.playerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BattleMessage {
    private MessageType type;
    private UserDTO user;
    private String battleId;
    private String opponentName;

    public enum MessageType {
        LOGIN, BATTLE_START, BATTLE_ACTION, BATTLE_END
    }

    public BattleMessage() {}

    public BattleMessage(MessageType type, UserDTO user, String battleId) {
        this.type = type;
        this.user = user;
        this.battleId = battleId;
    }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }

    public String getBattleId() { return battleId; }
    public void setBattleId(String battleId) { this.battleId = battleId; }

    public String getOpponentName() { return opponentName;}
    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }
}
