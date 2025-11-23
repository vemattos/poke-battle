package com.example.playerservice.controller;

import com.example.playerservice.model.Pokemon;
import com.example.playerservice.service.PokemonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/pokemon")
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<Pokemon> getPokemon(@PathVariable String name) {
        try {
            Pokemon pokemon = pokemonService.findAndSavePokemon(name);
            return ResponseEntity.ok(pokemon);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Pokemon> getPokemonById(@PathVariable int id) {
        Optional<Pokemon> pokemon = pokemonService.findById(id);
        return pokemon.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePokemon(@PathVariable int id) {
        try {
            pokemonService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}