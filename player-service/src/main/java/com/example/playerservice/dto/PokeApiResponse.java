package com.example.playerservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PokeApiResponse {
    private int id;
    private String name;
    private List<TypeSlot> types;
    private List<Stat> stats;
    private Sprites sprites;

    public Sprites getSprites() { return sprites; }
    public void setSprites(Sprites sprites) { this.sprites = sprites; }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getTypes() {
        return types.stream()
                .map(t -> t.getType().getName())
                .toList();
    }
    public void setTypes(List<TypeSlot> types) { this.types = types; }

    public List<Stat> getStats() { return stats; }
    public void setStats(List<Stat> stats) { this.stats = stats; }

    public String getPrimaryType() {
        if (types != null && !types.isEmpty()) {
            return types.get(0).getType().getName();
        }
        return "normal";
    }

    public int getHp() { return getStatValue("hp"); }
    public int getAttack() { return getStatValue("attack"); }
    public int getDefense() { return getStatValue("defense"); }

    private int getStatValue(String statName) {
        if (stats != null) {
            return stats.stream()
                    .filter(stat -> statName.equals(stat.getStat().getName()))
                    .map(Stat::getBase_stat)
                    .findFirst()
                    .orElse(50);
        }
        return 50;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TypeSlot {
        private Type type;

        public Type getType() { return type; }
        public void setType(Type type) { this.type = type; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Type {
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stat {
        private StatInfo stat;
        private int base_stat;

        public StatInfo getStat() { return stat; }
        public void setStat(StatInfo stat) { this.stat = stat; }

        public int getBase_stat() { return base_stat; }
        public void setBase_stat(int base_stat) { this.base_stat = base_stat; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StatInfo {
            private String name;
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sprites {

        @JsonProperty("back_default")
        private String backDefault;

        @JsonProperty("front_default")
        private String frontDefault;

        public String getBackDefault() { return backDefault; }
        public void setBackDefault(String backDefault) { this.backDefault = backDefault; }

        public String getFrontDefault() { return frontDefault; }
        public void setFrontDefault(String frontDefault) { this.frontDefault = frontDefault; }
    }

}
