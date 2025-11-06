package com.example.playerservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PokeApiResponse {
    private int id;
    private String name;
    private List<TypeSlot> types;
    private List<Stat> stats;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TypeSlot {
        private Type type;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Type {
            private String name;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stat {
        private StatInfo stat;
        private int base_stat;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class StatInfo {
            private String name;
        }
    }

    public String getPrimaryType() {
        if (types != null && !types.isEmpty()) {
            return types.get(0).getType().getName();
        }
        return "normal";
    }

    public int getHp() {
        return getStatValue("hp");
    }

    public int getAttack() {
        return getStatValue("attack");
    }

    public int getDefense() {
        return getStatValue("defense");
    }

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
}