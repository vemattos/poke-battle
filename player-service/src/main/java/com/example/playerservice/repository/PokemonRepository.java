package com.example.playerservice.repository;

import com.example.playerservice.model.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {
    Optional<Pokemon> findByNameIgnoreCase(String name);
}