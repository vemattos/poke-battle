package com.example.stadiumservice.dto;

import java.time.LocalDateTime;

public class ElectionMessage {
    private String instanceId;
    private String instanceRegion;
    private ElectionMessageType type;
    private LocalDateTime timestamp;

    public ElectionMessage() {}

    public ElectionMessage(String instanceId, String instanceRegion, ElectionMessageType type) {
        this.instanceId = instanceId;
        this.instanceRegion = instanceRegion;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public enum ElectionMessageType {
        HEARTBEAT,
        LEADER_ANNOUNCEMENT,
        ELECTION_START
    }

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
    public String getInstanceRegion() { return instanceRegion; }
    public void setInstanceRegion(String instanceRegion) { this.instanceRegion = instanceRegion; }
    public ElectionMessageType getType() { return type; }
    public void setType(ElectionMessageType type) { this.type = type; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}