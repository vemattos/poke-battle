package com.example.playerservice.dto;

public class StadiumStatusDTO {

    private int activeBattles;
    private String regionName;
    private int totalInstances;
    private String instanceId;
    private boolean isLeader;
    private int waitingPlayers;

    public StadiumStatusDTO() {}

    public StadiumStatusDTO(
            int activeBattles,
            String regionName,
            int totalInstances,
            String instanceId,
            boolean isLeader,
            int waitingPlayers
    ) {
        this.activeBattles = activeBattles;
        this.regionName = regionName;
        this.totalInstances = totalInstances;
        this.instanceId = instanceId;
        this.isLeader = isLeader;
        this.waitingPlayers = waitingPlayers;
    }

    public int getActiveBattles() {
        return activeBattles;
    }

    public void setActiveBattles(int activeBattles) {
        this.activeBattles = activeBattles;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public int getTotalInstances() {
        return totalInstances;
    }

    public void setTotalInstances(int totalInstances) {
        this.totalInstances = totalInstances;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    public int getWaitingPlayers() {
        return waitingPlayers;
    }

    public void setWaitingPlayers(int waitingPlayers) {
        this.waitingPlayers = waitingPlayers;
    }
}
