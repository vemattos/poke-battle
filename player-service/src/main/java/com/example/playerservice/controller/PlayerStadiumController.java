package com.example.playerservice.controller;

import com.example.playerservice.service.BattlePublisher;
import com.example.playerservice.service.StadiumDiscoveryService;
import com.example.playerservice.service.UserService;
import com.example.playerservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stadium")
public class PlayerStadiumController {

    private final BattlePublisher battlePublisher;
    private final UserService userService;
    private final StadiumDiscoveryService stadiumDiscoveryService;

    public PlayerStadiumController(BattlePublisher battlePublisher, UserService userService,
                                   StadiumDiscoveryService stadiumDiscoveryService) {
        this.battlePublisher = battlePublisher;
        this.userService = userService;
        this.stadiumDiscoveryService = stadiumDiscoveryService;
    }

    @PostMapping("/{userId}/enter")
    public ResponseEntity<Map<String, Object>> enterStadium(@PathVariable int userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            battlePublisher.sendLoginMessage(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Usuário " + user.getName() + " entrou no stadium",
                    "userId", userId,
                    "userName", user.getName()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableStadiums() {
        try {
            stadiumDiscoveryService.refreshStadiums();
            var stadiums = stadiumDiscoveryService.discoverAvailableStadiums();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "stadiums", stadiums,
                    "count", stadiums.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Erro ao buscar stadiums: " + e.getMessage(),
                    "stadiums", List.of(),
                    "count", 0
            ));
        }
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class StadiumEntryResponse {
    private boolean success;
    private String message;
    private Integer userId;
    private String userName;
    private String stadium;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class StadiumListResponse {
    private java.util.List<StadiumInfo> stadiums;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class StadiumInfo {
    private String code;
    private String name;
    private String queueName;
}