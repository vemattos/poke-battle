package com.example.playerservice.controller;

import com.example.playerservice.dto.CreateUserRequest;
import com.example.playerservice.dto.AddPokemonRequest;
import com.example.playerservice.dto.Stadium;
import com.example.playerservice.model.User;
import com.example.playerservice.service.BattlePublisher;
import com.example.playerservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final BattlePublisher battlePublisher;

    public UserController(UserService userService, BattlePublisher battlePublisher) {
        this.userService = userService;
        this.battlePublisher = battlePublisher;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{userId}/enter-stadium")
    public ResponseEntity<StadiumEntryResponse> enterStadium(@PathVariable int userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Stadium stadium = Stadium.getRandom();
            battlePublisher.sendLoginMessage(user, stadium);

            StadiumEntryResponse response = new StadiumEntryResponse();
            response.setSuccess(true);
            response.setMessage("Usuário " + user.getName() + " entrou no " + stadium.getName());
            response.setUserId(userId);
            response.setUserName(user.getName());
            response.setStadium(stadium.getName());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            StadiumEntryResponse response = new StadiumEntryResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}