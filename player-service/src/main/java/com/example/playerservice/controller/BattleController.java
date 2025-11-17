package com.example.playerservice.controller;

import com.example.playerservice.dto.BattleMessage;
import com.example.playerservice.dto.PokemonDTO;
import com.example.playerservice.dto.UserDTO;
import com.example.playerservice.model.Pokemon;
import com.example.playerservice.model.User;
import com.example.playerservice.service.BattlePublisher;
import com.example.playerservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/battle")
public class BattleController {

    private final BattlePublisher battlePublisher;
    private final UserService userService;


    public BattleController(BattlePublisher battlePublisher, UserService userService) {
        this.battlePublisher = battlePublisher;
        this.userService = userService;
    }

    @PostMapping("/{userId}/attack")
    public ResponseEntity<String> attack(@PathVariable int userId, @RequestParam String battleId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            BattleMessage message = new BattleMessage();
            message.setType(BattleMessage.MessageType.PLAYER_ACTION);
            message.setUser(convertToDTO(user));
            message.setBattleId(battleId);
            message.setAction(BattleMessage.BattleAction.ATTACK);

            battlePublisher.sendBattleAction(message);
            return ResponseEntity.ok("Ataque enviado para a batalha " + battleId + "!");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/switch")
    public ResponseEntity<String> switchPokemon(
            @PathVariable int userId,
            @RequestParam String battleId,
            @RequestParam int pokemonIndex) {

        return sendBattleAction(userId, battleId, BattleMessage.BattleAction.SWITCH_POKEMON, pokemonIndex);
    }

    private ResponseEntity<String> sendBattleAction(int userId, String battleId,
                                                    BattleMessage.BattleAction action, Integer target) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            BattleMessage message = new BattleMessage();
            message.setType(BattleMessage.MessageType.PLAYER_ACTION);
            message.setUser(convertToDTO(user));
            message.setBattleId(battleId);
            message.setAction(action);
            message.setTarget(target);

            battlePublisher.sendBattleAction(message);

            String actionText = action == BattleMessage.BattleAction.ATTACK ? "Ataque" : "Troca de Pokémon";
            return ResponseEntity.ok(actionText + " enviado para a batalha " + battleId + "!");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/team")
    public ResponseEntity<List<PokemonDTO>> getUserTeam(@PathVariable int userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            List<PokemonDTO> team = user.getTeam().stream()
                    .map(this::convertPokemonToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(team);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{userId}/flee")
    public ResponseEntity<String> flee(@PathVariable int userId, @RequestParam String battleId) {
        return sendBattleAction(userId, battleId, BattleMessage.BattleAction.FLEE, null);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setTeam(user.getTeam().stream().map(this::convertPokemonToDTO).collect(Collectors.toList()));
        return userDTO;
    }

    private PokemonDTO convertPokemonToDTO(Pokemon pokemon) {
        PokemonDTO dto = new PokemonDTO();
        dto.setId(pokemon.getId());
        dto.setName(pokemon.getName());
        dto.setType(pokemon.getType());
        dto.setHp(pokemon.getHp());
        dto.setAttack(pokemon.getAttack());
        dto.setDefense(pokemon.getDefense());
        dto.setCurrentHp(pokemon.getCurrentHp());
        return dto;
    }
}