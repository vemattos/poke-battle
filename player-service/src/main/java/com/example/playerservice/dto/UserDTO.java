package com.example.playerservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import java.util.List;

public class UserDTO {
    private int id;
    private String name;
    private List<PokemonDTO> team;

    public UserDTO() {}

    public UserDTO(int id, String name, List<PokemonDTO> team) {
        this.id = id;
        this.name = name;
        this.team = team;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<PokemonDTO> getTeam() { return team; }
    public void setTeam(List<PokemonDTO> team) { this.team = team; }
}
