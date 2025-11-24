package com.example.stadiumservice.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StadiumService {

    private final Map<String, BattleService> instanceServices = new ConcurrentHashMap<>();
    private String currentInstanceId;

    public StadiumService() {
        System.out.println("StadiumService inicializado");
    }

    public void registerBattleService(String instanceId, BattleService battleService) {
        instanceServices.put(instanceId, battleService);
        this.currentInstanceId = instanceId;

        System.out.println("BattleService registrado: " + instanceId +
                " | Região: " + battleService.getRegionName());
    }

    public Map<String, BattleService> getAllInstanceServices() {
        return new ConcurrentHashMap<>(instanceServices);
    }

    public void setElectionInfo(boolean isLeader, String leaderInstanceId) {
        System.out.println("Info de eleição atualizada | Líder: " + leaderInstanceId + " | Eu sou líder: " + isLeader);
    }

    public BattleService getCurrentInstanceService() {
        return instanceServices.get(currentInstanceId);
    }

    public String getCurrentInstanceId() {
        return currentInstanceId;
    }

    public int getWaitingPlayersCount() {
        BattleService service = getCurrentInstanceService();
        return service != null ? service.getWaitingPlayersCount() : 0;
    }

    public int getActiveBattlesCount() {
        BattleService service = getCurrentInstanceService();
        return service != null ? service.getActiveBattlesCount() : 0;
    }
}