package com.example.playerservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    public void addPokemonToTeam(Pokemon pokemon) {
        if (this.team == null) {
            this.team = new ArrayList<>();
        }
        if (this.team.size() < 6) {
            this.team.add(pokemon);
        } else {
            throw new RuntimeException("Time já está completo (máximo 6 Pokémon)");
        }
    }

    public void removePokemonFromTeam(Pokemon pokemon) {
        if (this.team != null) {
            this.team.remove(pokemon);
        }
    }
}