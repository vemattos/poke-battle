package com.example.stadiumservice.controller;

import com.example.stadiumservice.service.ElectionService;
import com.example.stadiumservice.service.StadiumService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stadium")
public class StadiumController {

    private final StadiumService stadiumService;
    private final ElectionService electionService;

    public StadiumController(StadiumService stadiumService, ElectionService electionService) {
        this.stadiumService = stadiumService;
        this.electionService = electionService;
    }

    @GetMapping("/status")
    public Map<String, Object> getStadiumStatus() {
        return Map.of(
                "instanceId", stadiumService.getCurrentInstanceId(),
                "regionName", stadiumService.getCurrentInstanceService().getRegionName(),
                "isLeader", electionService.isLeader(),
                "leaderInstanceId", electionService.getCurrentLeader(),
                "waitingPlayers", stadiumService.getWaitingPlayersCount(),
                "activeBattles", stadiumService.getActiveBattlesCount()
        );
    }

    @GetMapping("/active")
    public Object getActiveBattles() {
        return stadiumService.getCurrentInstanceService().getActiveBattles();
    }

    @GetMapping("/waiting")
    public Object getWaitingPlayers() {
        return stadiumService.getCurrentInstanceService().getWaitingPlayers();
    }
}