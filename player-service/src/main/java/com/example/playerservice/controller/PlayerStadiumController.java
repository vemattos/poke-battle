package com.example.playerservice.controller;

import com.example.playerservice.model.User;
import com.example.playerservice.service.BattlePublisher;
import com.example.playerservice.service.UserService;
import com.example.playerservice.dto.Stadium;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stadium")
public class PlayerStadiumController {

    private final BattlePublisher battlePublisher;
    private final UserService userService;

    public PlayerStadiumController(BattlePublisher battlePublisher, UserService userService) {
        this.battlePublisher = battlePublisher;
        this.userService = userService;
    }

    @PostMapping("/{userId}/enter/{stadiumName}")
    public ResponseEntity<StadiumEntryResponse> enterSpecificStadium(
            @PathVariable int userId,
            @PathVariable String stadiumName) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Stadium stadium = Stadium.valueOf(stadiumName.toUpperCase());
            battlePublisher.sendLoginMessage(user, stadium);

            StadiumEntryResponse response = new StadiumEntryResponse();
            response.setSuccess(true);
            response.setMessage("Usuário " + user.getName() + " entrou no " + stadium.getName());
            response.setUserId(userId);
            response.setUserName(user.getName());
            response.setStadium(stadium.getName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            StadiumEntryResponse response = new StadiumEntryResponse();
            response.setSuccess(false);
            response.setMessage("Stadium inválido: " + stadiumName + ". Opções: STADIUM_1, STADIUM_2, STADIUM_3");
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            StadiumEntryResponse response = new StadiumEntryResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/available")
    public ResponseEntity<StadiumListResponse> getAvailableStadiums() {
        Stadium[] stadiums = Stadium.values();

        StadiumListResponse response = new StadiumListResponse();
        response.setStadiums(java.util.Arrays.stream(stadiums)
                .map(stadium -> new StadiumInfo(stadium.name(), stadium.getName(), stadium.getQueueName()))
                .toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/enter")
    public ResponseEntity<StadiumEntryResponse> enterRandomStadium(@PathVariable int userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            Stadium stadium = Stadium.getRandom();
            battlePublisher.sendLoginMessage(user, stadium);

            StadiumEntryResponse response = new StadiumEntryResponse();
            response.setSuccess(true);
            response.setMessage("Usuário " + user.getName() + " entrou no " + stadium.getName() + " (aleatório)");
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