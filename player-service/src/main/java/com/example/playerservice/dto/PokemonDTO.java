package com.example.playerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PokemonDTO {
    private int id;
    private String name;
    private String type;
    private int hp;
    private int attack;
    private int defense;
    private int currentHp;
}