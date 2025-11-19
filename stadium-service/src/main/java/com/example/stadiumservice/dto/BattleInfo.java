package com.example.stadiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BattleInfo {
    private String battleId;
    private UserDTO player1;
    private UserDTO player2;
    private String status;
}