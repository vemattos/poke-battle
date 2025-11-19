package com.example.stadiumservice.controller;

import com.example.stadiumservice.dto.Stadium;
import com.example.stadiumservice.dto.StadiumStatus;
import com.example.stadiumservice.dto.UserDTO;
import com.example.stadiumservice.service.StadiumService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stadium")
public class StadiumController {

    private final StadiumService stadiumService;

    public StadiumController(StadiumService stadiumService) {
        this.stadiumService = stadiumService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<Stadium, StadiumStatus>> getAllStadiumsStatus() {
        try {
            Map<Stadium, StadiumStatus> allStatus = stadiumService.getAllStadiumsStatus();
            return ResponseEntity.ok(allStatus);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{stadiumName}/status")
    public ResponseEntity<StadiumStatus> getStadiumStatus(@PathVariable String stadiumName) {
        try {
            Stadium stadium = Stadium.valueOf(stadiumName.toUpperCase().replace("-", "_"));
            StadiumStatus status = stadiumService.getStadiumStatus(stadium);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<Stadium>> getAvailableStadiums() {
        try {
            List<Stadium> stadiums = stadiumService.getAvailableStadiums();
            return ResponseEntity.ok(stadiums);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/best")
    public ResponseEntity<Stadium> getBestStadium() {
        try {
            Stadium bestStadium = stadiumService.getAvailableStadiums().stream()
                    .min(Comparator.comparing(stadium ->
                            stadiumService.getStadiumStatus(stadium).getWaitingPlayersCount()))
                    .orElse(Stadium.STADIUM_1);

            return ResponseEntity.ok(bestStadium);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{stadiumName}/waiting-opponent")
    public ResponseEntity<StadiumMatchStatus> checkForOpponent(@PathVariable String stadiumName) {
        try {
            Stadium stadium = Stadium.valueOf(stadiumName.toUpperCase().replace("-", "_"));
            var battleService = stadiumService.getStadiumService(stadium);

            if (battleService == null) {
                return ResponseEntity.notFound().build();
            }

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