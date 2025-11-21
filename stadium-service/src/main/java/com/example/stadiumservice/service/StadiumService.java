package com.example.stadiumservice.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StadiumService {

    private final Map<String, BattleService> instanceServices = new ConcurrentHashMap<>();
    private String currentInstanceId;

    public StadiumService() {
        // Construtor vazio - as instâncias serão registradas via registerBattleService
    }

    public void registerBattleService(String instanceId, BattleService battleService) {
        instanceServices.put(instanceId, battleService);
        this.currentInstanceId = instanceId;
        System.out.println("✅ BattleService registrado: " + instanceId +
                " | Região: " + battleService.getRegionName());
    }

    public BattleService getCurrentInstanceService() {
        return instanceServices.get(currentInstanceId);
    }

    public String getCurrentInstanceId() {
        return currentInstanceId;
    }

    public boolean isLeader() {
        // Lógica simples de eleição
        return currentInstanceId != null &&
                currentInstanceId.equals(instanceServices.keySet().stream().sorted().findFirst().orElse(currentInstanceId));
    }

    public Map<String, BattleService> getAllInstanceServices() {
        return instanceServices;
    }

    public int getTotalInstances() {
        return instanceServices.size();
    }
}