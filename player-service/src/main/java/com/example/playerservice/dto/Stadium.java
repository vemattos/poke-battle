package com.example.playerservice.dto;

public enum Stadium {
    STADIUM_1("Stadium 1", "stadium-1"),
    STADIUM_2("Stadium 2", "stadium-2"),
    STADIUM_3("Stadium 3", "stadium-3");

    private final String name;
    private final String queueName;

    Stadium(String name, String queueName) {
        this.name = name;
        this.queueName = queueName;
    }

    public String getName() { return name; }
    public String getQueueName() { return queueName; }

    public static Stadium getRandom() {
        Stadium[] stadiums = values();
        return stadiums[(int) (Math.random() * stadiums.length)];
    }
}