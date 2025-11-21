package com.example.stadiumservice.dto;

public enum PokemonRegion {
    HOENN("Hoenn", "battle.request.queue.stadium-hoenn"),
    UNOVA("Unova", "battle.request.queue.stadium-unova"),
    KALOS("Kalos", "battle.request.queue.stadium-kalos");

    private final String name;
    private final String queueName;

    PokemonRegion(String name, String queueName) {
        this.name = name;
        this.queueName = queueName;
    }

    public String getName() {
        return name;
    }

    public String getQueueName() {
        return queueName;
    }

    public static PokemonRegion getRandom() {
        PokemonRegion[] regions = values();
        return regions[(int) (Math.random() * regions.length)];
    }
}