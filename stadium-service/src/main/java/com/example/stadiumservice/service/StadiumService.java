package com.example.stadiumservice.service;

import com.example.stadiumservice.dto.Stadium;
import com.example.stadiumservice.dto.StadiumStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class StadiumService {

    private final Map<Stadium, BattleService> stadiumServices = new ConcurrentHashMap<>();
    private final BattleEngine battleEngine;

    public StadiumService(BattleEngine battleEngine) {
        this.battleEngine = battleEngine;
    }

    public void registerBattleService(Stadium stadium, BattleService battleService) {
        stadiumServices.put(stadium, battleService);
    }

    public BattleService getStadiumService(Stadium stadium) {
        return stadiumServices.get(stadium);
    }

    public List<Stadium> getAvailableStadiums() {
        return List.of(Stadium.values());
    }

    public StadiumStatus getStadiumStatus(Stadium stadium) {
        BattleService battleService = stadiumServices.get(stadium);
        if (battleService == null) {
            StadiumStatus status = new StadiumStatus();
            status.setStadium(stadium);
            status.setWaitingPlayersCount(0);
            status.setActiveBattlesCount(0);
            return status;
        }

        StadiumStatus status = new StadiumStatus();
        status.setStadium(stadium);
        status.setWaitingPlayersCount(battleService.getWaitingPlayersCount());
        status.setWaitingPlayers(battleService.getWaitingPlayers());
        status.setActiveBattlesCount(battleService.getActiveBattlesCount());
        status.setActiveBattles(battleService.getActiveBattles());
        return status;
    }

    public Map<Stadium, StadiumStatus> getAllStadiumsStatus() {
        return stadiumServices.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> getStadiumStatus(entry.getKey())
                ));
    }
}