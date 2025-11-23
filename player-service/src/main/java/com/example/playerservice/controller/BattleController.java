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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/battle")
public class BattleController {

    private final BattlePublisher battlePublisher;
    private final UserService userService;

    // Mapa para guardar em qual instância cada batalha está ocorrendo
    private final Map<String, String> battleInstances = new ConcurrentHashMap<>();

    public BattleController(BattlePublisher battlePublisher, UserService userService) {
        this.battlePublisher = battlePublisher;
        this.userService = userService;
    }

    // Método para registrar onde uma batalha está ocorrendo
    public void registerBattleInstance(String battleId, String instanceId) {
        battleInstances.put(battleId, instanceId);
        System.out.println("Batalha registrada: " + battleId + " → Instância: " + instanceId);
    }

    @PostMapping("/{userId}/attack")
    public ResponseEntity<Map<String, Object>> attack(@PathVariable int userId, @RequestParam String battleId) {
        return sendBattleAction(userId, battleId, BattleMessage.BattleAction.ATTACK, null);
    }

    @PostMapping("/{userId}/switch")
    public ResponseEntity<Map<String, Object>> switchPokemon(
            @PathVariable int userId,
            @RequestParam String battleId,
            @RequestParam int pokemonIndex) {
        return sendBattleAction(userId, battleId, BattleMessage.BattleAction.SWITCH_POKEMON, pokemonIndex);
    }

    @PostMapping("/{userId}/flee")
    public ResponseEntity<Map<String, Object>> flee(@PathVariable int userId, @RequestParam String battleId) {
        return sendBattleAction(userId, battleId, BattleMessage.BattleAction.FLEE, null);
    }

    private ResponseEntity<Map<String, Object>> sendBattleAction(int userId, String battleId,
                                                                 BattleMessage.BattleAction action, Integer target) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // ✅ CORREÇÃO: Extrair instanceId do battleId
            String instanceId = extractInstanceIdFromBattleId(battleId);
            if (instanceId == null) {
                // Tenta buscar do mapa de instâncias
                instanceId = battleInstances.get(battleId);
                if (instanceId == null) {
                    throw new RuntimeException("InstanceId não encontrado para a batalha: " + battleId);
                }
            }

            BattleMessage message = new BattleMessage();
            message.setType(BattleMessage.MessageType.PLAYER_ACTION);
            message.setUser(convertToDTO(user));
            message.setBattleId(battleId);
            message.setAction(action);
            message.setTarget(target);
            message.setInstanceId(instanceId);

            battlePublisher.sendBattleAction(message);

            String actionText = getActionText(action);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", actionText + " enviado para a batalha " + battleId,
                    "battleId", battleId,
                    "instanceId", instanceId
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // NOVO MÉTODO: Extrair instanceId do battleId
    private String extractInstanceIdFromBattleId(String battleId) {
        if (battleId == null) return null;

        // Formato: battle-1763731387622-stadium-instance-f5744905-bd9b-45f6-90e6-f717007d956a
        String[] parts = battleId.split("-");
        if (parts.length >= 4) {
            // Junta as partes a partir de "stadium-instance"
            StringBuilder instanceId = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                if (instanceId.length() > 0) instanceId.append("-");
                instanceId.append(parts[i]);
            }
            return instanceId.toString();
        }
        return null;
    }

    private String getActionText(BattleMessage.BattleAction action) {
        switch (action) {
            case ATTACK: return "Ataque";
            case SWITCH_POKEMON: return "Troca de Pokémon";
            case FLEE: return "Fuga";
            default: return "Ação";
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

    @GetMapping("/instances")
    public Map<String, String> getBattleInstances() {
        return new HashMap<>(battleInstances);
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
        dto.setType1(pokemon.getType1());
        dto.setType2(pokemon.getType2());
        dto.setHp(pokemon.getHp());
        dto.setAttack(pokemon.getAttack());
        dto.setDefense(pokemon.getDefense());
        dto.setCurrentHp(pokemon.getCurrentHp());
        return dto;
    }
}