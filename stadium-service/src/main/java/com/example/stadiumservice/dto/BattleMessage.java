package com.example.stadiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BattleMessage {
    private MessageType type;
    private UserDTO user;
    private String battleId;
    private String opponentName;

    public enum MessageType {
        LOGIN, BATTLE_START, BATTLE_ACTION, BATTLE_END
    }
}