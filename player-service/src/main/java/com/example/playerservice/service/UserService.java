package com.example.playerservice.service;

import com.example.playerservice.model.Pokemon;
import com.example.playerservice.model.User;
import com.example.playerservice.repository.PokemonRepository;
import com.example.playerservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PokemonRepository pokemonRepository;

    public UserService(UserRepository userRepository, PokemonRepository pokemonRepository) {
        this.userRepository = userRepository;
        this.pokemonRepository = pokemonRepository;
    }

    public User createUser(String name) {
        if (userRepository.existsByName(name)) {
            return getUserByName(name);
        }

        User user = new User();
        user.setName(name);
        return userRepository.save(user);
    }

    public User addPokemonToTeam(int userId, int pokemonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Pokemon pokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new RuntimeException("Pokémon não encontrado"));

        user.addPokemonToTeam(pokemon);
        return userRepository.save(user);
    }

    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    public User getUserByName(String name) {
        return userRepository.findByName(name);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User removePokemonFromTeam(int userId, int pokemonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Pokemon pokemon = pokemonRepository.findById(pokemonId)
                .orElseThrow(() -> new RuntimeException("Pokémon não encontrado"));

        user.removePokemonFromTeam(pokemon);
        return userRepository.save(user);
    }

    public void deleteUser(int userId) {
        userRepository.deleteById(userId);
    }
}