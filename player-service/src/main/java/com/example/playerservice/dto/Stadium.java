package com.example.playerservice.dto;

public class Stadium {
    private String instanceId;
    private String name;
    private String queueName;
    private int waitingPlayers;
    private int activeBattles;
    private String instanceUrl;

    public Stadium() {}

    public Stadium(String instanceId, String name, String queueName, int waitingPlayers, int activeBattles, String instanceUrl) {
        this.instanceId = instanceId;
        this.name = name;
        this.queueName = queueName;
        this.waitingPlayers = waitingPlayers;
        this.activeBattles = activeBattles;
        this.instanceUrl = instanceUrl;
    }

    // Getters e Setters
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }

    public int getWaitingPlayers() { return waitingPlayers; }
    public void setWaitingPlayers(int waitingPlayers) { this.waitingPlayers = waitingPlayers; }

    public int getActiveBattles() { return activeBattles; }
    public void setActiveBattles(int activeBattles) { this.activeBattles = activeBattles; }

    public String getInstanceUrl() { return instanceUrl; }
    public void setInstanceUrl(String instanceUrl) { this.instanceUrl = instanceUrl; }
}