package com.example.stadiumservice.service;

import com.example.stadiumservice.dto.PokemonDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class BattleEngine {

    private static final Map<String, Map<String, Double>> TYPE_ADVANTAGES = Map.of(
            "fire", Map.of("grass", 2.0, "water", 0.5, "fire", 1.0, "electric", 1.0),
            "water", Map.of("fire", 2.0, "grass", 0.5, "water", 1.0, "electric", 1.0),
            "grass", Map.of("water", 2.0, "fire", 0.5, "grass", 1.0, "electric", 1.0),
            "electric", Map.of("water", 2.0, "grass", 1.0, "fire", 1.0, "electric", 1.0),
            "normal", Map.of("normal", 1.0, "fire", 1.0, "water", 1.0, "grass", 1.0, "electric", 1.0)
    );

    public BattleResult calculateBattle(PokemonDTO attacker, PokemonDTO defender) {
        double typeMultiplier = getTypeMultiplier(attacker.getType(), defender.getType());

        int baseDamage = Math.max(1, attacker.getAttack() - (defender.getDefense() / 2));
        int finalDamage = (int) (baseDamage * typeMultiplier);

        String log = attacker.getName() + " atacou " + defender.getName() + "!";
        if (typeMultiplier > 1.0) {
            log += " Foi super efetivo!";
        } else if (typeMultiplier < 1.0) {
            log += " Não foi muito efetivo...";
        }

        log += " Dano: " + finalDamage;

        return new BattleResult(finalDamage, log);
    }

    private double getTypeMultiplier(String attackerType, String defenderType) {
        return TYPE_ADVANTAGES
                .getOrDefault(attackerType, Map.of("normal", 1.0))
                .getOrDefault(defenderType, 1.0);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BattleResult {
        private int damage;
        private String log;
    }
}