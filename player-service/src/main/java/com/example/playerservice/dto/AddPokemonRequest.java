package com.example.playerservice.dto;

import lombok.Data;

public class AddPokemonRequest {
    private int pokemonId;

    public int getPokemonId() { return pokemonId; }
    public void setPokemonId(int pokemonId) { this.pokemonId = pokemonId; }
}
