package com.example.playerservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "user_pokemon_team",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "pokemon_id")
    )
    private List<Pokemon> team = new ArrayList<>();

    public User() {}

    public User(int id, String name, List<Pokemon> team) {
        this.id = id;
        this.name = name;
        this.team = team;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Pokemon> getTeam() { return team; }
    public void setTeam(List<Pokemon> team) { this.team = team; }

    public void addPokemonToTeam(Pokemon pokemon) {
        if (this.team == null) this.team = new ArrayList<>();
        if (this.team.size() < 6) this.team.add(pokemon);
        else throw new RuntimeException("Time já está completo (máximo 6 Pokémon)");
    }

    public void removePokemonFromTeam(Pokemon pokemon) {
        if (this.team != null) this.team.remove(pokemon);
    }
}
