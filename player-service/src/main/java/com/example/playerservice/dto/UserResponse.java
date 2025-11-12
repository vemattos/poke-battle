package com.example.playerservice.dto;

import com.example.playerservice.model.Pokemon;
import lombok.Data;

import java.util.List;

import java.util.List;

public class UserResponse {
    private int id;
    private String name;
    private List<Pokemon> team;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Pokemon> getTeam() { return team; }
    public void setTeam(List<Pokemon> team) { this.team = team; }
}
