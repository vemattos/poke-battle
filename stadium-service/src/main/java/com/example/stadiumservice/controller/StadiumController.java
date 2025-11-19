package com.example.stadiumservice.controller;

import com.example.stadiumservice.dto.StadiumStatus;
import com.example.stadiumservice.dto.UserDTO;
import com.example.stadiumservice.service.BattleService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stadium")
public class StadiumController {

    private final BattleService battleService;

    public StadiumController(BattleService battleService) {
        this.battleService = battleService;
    }

    @GetMapping("/status")
    public ResponseEntity<StadiumStatus> getStadiumStatus() {
        try {
            StadiumStatus status = new StadiumStatus();
            status.setWaitingPlayersCount(battleService.getWaitingPlayersCount());
            status.setWaitingPlayers(battleService.getWaitingPlayers());
            status.setActiveBattlesCount(battleService.getActiveBattlesCount());
            status.setActiveBattles(battleService.getActiveBattles());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/waiting-opponent")
    public ResponseEntity<StadiumMatchStatus> checkForOpponent() {
        try {
            int waitingPlayers = battleService.getWaitingPlayersCount();

            StadiumMatchStatus matchStatus = new StadiumMatchStatus();
            matchStatus.setHasOpponent(waitingPlayers > 0);

            if (waitingPlayers > 0) {
                matchStatus.setOpponent(battleService.getWaitingPlayers().get(0));
                matchStatus.setMessage("Oponente encontrado! Pronto para batalhar.");
            } else {
                matchStatus.setMessage("Nenhum oponente encontrado. Aguardando...");
            }

            return ResponseEntity.ok(matchStatus);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class StadiumMatchStatus {
    private boolean hasOpponent;
    private UserDTO opponent;
    private String message;
}
