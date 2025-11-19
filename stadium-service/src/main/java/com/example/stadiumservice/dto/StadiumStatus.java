package com.example.stadiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StadiumStatus {
    private Stadium stadium;
    private int waitingPlayersCount;
    private List<UserDTO> waitingPlayers;
    private int activeBattlesCount;
    private List<BattleInfo> activeBattles;
}

