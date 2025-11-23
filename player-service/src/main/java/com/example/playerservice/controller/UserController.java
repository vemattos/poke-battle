package com.example.playerservice.controller;

import com.example.playerservice.dto.CreateUserRequest;
import com.example.playerservice.dto.AddPokemonRequest;
import com.example.playerservice.model.User;
import com.example.playerservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request.getName());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/team")
    public ResponseEntity<User> addPokemonToTeam(
            @PathVariable int userId,
            @RequestBody AddPokemonRequest request) {
        try {
            User user = userService.addPokemonToTeam(userId, request.getPokemonId());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}/team/{pokemonId}")
    public ResponseEntity<User> removePokemonFromTeam(
            @PathVariable int userId,
            @PathVariable int pokemonId) {
        try {
            User user = userService.removePokemonFromTeam(userId, pokemonId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}