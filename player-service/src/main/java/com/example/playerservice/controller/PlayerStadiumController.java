package com.example.playerservice.controller;

import com.example.playerservice.model.User;
import com.example.playerservice.service.BattlePublisher;
import com.example.playerservice.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stadium")
public class PlayerStadiumController {

    private final BattlePublisher battlePublisher;
    private final UserService userService;

    public PlayerStadiumController(BattlePublisher battlePublisher, UserService userService) {
        this.battlePublisher = battlePublisher;
        this.userService = userService;
    }

    @GetMapping("/{userId}/enter")
    public ResponseEntity<StadiumEntryResponse> enterStadium(@PathVariable int userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            battlePublisher.sendLoginMessage(user);

            StadiumEntryResponse response = new StadiumEntryResponse();
            response.setSuccess(true);
            response.setMessage("Usuário " + user.getName() + " entrou no stadium");
            response.setUserId(userId);
            response.setUserName(user.getName());

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
}