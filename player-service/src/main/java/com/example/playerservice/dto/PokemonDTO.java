package com.example.playerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PokemonDTO {
    private int id;
    private String name;
    private String type;
    private int hp;
    private int attack;
    private int defense;
    private int currentHp;

    public PokemonDTO() {}

    public PokemonDTO(int id, String name, String type, int hp, int attack, int defense, int currentHp) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.currentHp = currentHp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
}
