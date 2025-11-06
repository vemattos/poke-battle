package com.example.playerservice.service;

import com.example.playerservice.dto.PokeApiResponse;
import com.example.playerservice.model.Pokemon;
import com.example.playerservice.repository.PokemonRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PokemonService {

    private final PokemonRepository pokemonRepository;
    private final PokeApiService pokeApiService;

    public PokemonService(PokemonRepository pokemonRepository, PokeApiService pokeApiService) {
        this.pokemonRepository = pokemonRepository;
        this.pokeApiService = pokeApiService;
    }

    public Pokemon findAndSavePokemon(String name) {
        Optional<Pokemon> existingPokemon = pokemonRepository.findByNameIgnoreCase(name);
        if (existingPokemon.isPresent()) {
            return existingPokemon.get();
        }

        PokeApiResponse apiResponse = pokeApiService.getPokemonFromApi(name);

        Pokemon pokemon = pokeApiService.convertToPokemonEntity(apiResponse);
        return pokemonRepository.save(pokemon);
    }

    public List<Pokemon> findAll() {
        return pokemonRepository.findAll();
    }

    public Optional<Pokemon> findById(int id) {
        return pokemonRepository.findById(id);
    }

    public Optional<Pokemon> findByName(String name) {
        return pokemonRepository.findByNameIgnoreCase(name);
    }

    public void deleteById(int id) {
        pokemonRepository.deleteById(id);
    }
}