package com.example.playerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public PokemonDTO() {}

    public PokemonDTO(int id, String name, String type, int hp, int attack, int defense, int currentHp, String frontSprite, String backSprite) {
        this.id = id;
        this.name = name;
        this.type1 = type1;
        this.type2 = type2;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.currentHp = currentHp;
        this.frontSprite = frontSprite;
        this.backSprite = backSprite;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType1() { return type1; }
    public void setType1(String type1) { this.type1 = type1; }

    public String getType2() { return type2; }
    public void setType2(String type2) { this.type2 = type2; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public String getFrontSprite() { return frontSprite; }
    public void setFrontSprite(String frontSprite) { this.frontSprite = frontSprite; }

    public String getBackSprite() { return backSprite; }
    public void setBackSprite(String backSprite) { this.backSprite = backSprite; }
}
