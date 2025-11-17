package com.example.stadiumservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PokemonDTO {
    private int id;
    private String name;
    private String type1;
    private String type2;
    private int hp;
    private int attack;
    private int defense;
    private int currentHp;
    private String frontSprite;
    private String backSprite;
    public boolean isFainted() {
        return this.currentHp <= 0;
    }
}
