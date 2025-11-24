package com.example.stadiumservice.controller;

import com.example.stadiumservice.service.ElectionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/election")
public class ElectionController {

    private final ElectionService electionService;

    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @GetMapping("/status")
    public Map<String, Object> getElectionStatus() {
        return Map.of(
                "currentInstanceId", electionService.getCurrentInstanceId(),
                "isLeader", electionService.isLeader(),
                "leaderInstanceId", electionService.getCurrentLeader(),
                "message", "Sistema de eleição ativo"
        );
    }

    @PostMapping("/force")
    public Map<String, Object> forceElection() {
        electionService.forceElection();
        return Map.of(
                "success", true,
                "message", "Eleição forçada iniciada",
                "instanceId", electionService.getCurrentInstanceId()
        );
    }

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        return Map.of(
                "status", "HEALTHY",
                "instanceId", electionService.getCurrentInstanceId(),
                "isLeader", electionService.isLeader(),
                "timestamp", System.currentTimeMillis()
        );
    }
}