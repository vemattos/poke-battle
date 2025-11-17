package com.example.playerservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String type;
    private int hp;
    private int attack;
    private int defense;

    public Pokemon() {}

    public Pokemon(int id, String name, String type, int hp, int attack, int defense) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
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

    @Transient
    private int currentHp;

    public void prepareForBattle() {
        this.currentHp = this.hp;
    }

    public int getCurrentHp() {
        return currentHp > 0 ? currentHp : hp;
    }

    public boolean isFainted() {
        return currentHp <= 0;
    }
}
