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
        double typeMultiplier = calculateTypeMultiplier(attacker, defender);

        int baseDamage = Math.max(1, attacker.getAttack() - (defender.getDefense() / 2));
        int finalDamage = (int) (baseDamage * typeMultiplier);

        String log = buildBattleLog(attacker, defender, finalDamage, typeMultiplier);

        return new BattleResult(finalDamage, log);
    }

    private double calculateTypeMultiplier(PokemonDTO attacker, PokemonDTO defender) {
        double multiplier1 = getTypeMultiplier(attacker.getType1(), defender.getType1());
        double multiplier2 = getTypeMultiplier(attacker.getType1(), defender.getType2());
        double multiplier3 = getTypeMultiplier(attacker.getType2(), defender.getType1());
        double multiplier4 = getTypeMultiplier(attacker.getType2(), defender.getType2());

        double finalMultiplier = Math.max(Math.max(multiplier1, multiplier2),
                Math.max(multiplier3, multiplier4));

        return finalMultiplier;
    }

    private double getTypeMultiplier(String attackerType, String defenderType) {
        if (attackerType == null || defenderType == null) {
            return 1.0;
        }

        return TYPE_ADVANTAGES
                .getOrDefault(attackerType, Map.of("normal", 1.0))
                .getOrDefault(defenderType, 1.0);
    }

    private String buildBattleLog(PokemonDTO attacker, PokemonDTO defender, int damage, double multiplier) {
        String log = attacker.getName() + " atacou " + defender.getName() + "!";

        if (multiplier > 1.0) {
            log += " Foi super efetivo!";
        } else if (multiplier < 1.0) {
            log += " Nao foi muito efetivo...";
        }

        log += " Dano: " + damage;
        return log;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BattleResult {
        private int damage;
        private String log;
    }
}