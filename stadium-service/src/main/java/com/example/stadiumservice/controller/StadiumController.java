package com.example.stadiumservice.controller;

import com.example.stadiumservice.service.StadiumService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stadium")
public class StadiumController {

    private final StadiumService stadiumService;

    public StadiumController(StadiumService stadiumService) {
        this.stadiumService = stadiumService;
    }

    @GetMapping("/status")
    public Map<String, Object> getStadiumStatus() {
        var currentService = stadiumService.getCurrentInstanceService();

        return Map.of(
                "instanceId", stadiumService.getCurrentInstanceId(),
                "regionName", currentService != null ? currentService.getRegionName() : "Unknown",
                "waitingPlayers", currentService != null ? currentService.getWaitingPlayersCount() : 0,
                "activeBattles", currentService != null ? currentService.getActiveBattlesCount() : 0,
                "isLeader", stadiumService.isLeader(),
                "totalInstances", stadiumService.getTotalInstances()
        );
    }
}

