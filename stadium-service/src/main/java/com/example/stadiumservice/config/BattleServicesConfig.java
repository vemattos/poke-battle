package com.example.stadiumservice.config;

import com.example.stadiumservice.dto.PokemonRegion;
import com.example.stadiumservice.service.BattleEngine;
import com.example.stadiumservice.service.BattleService;
import com.example.stadiumservice.service.StadiumService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BattleServicesConfig {

    @Value("${pokemon.region:HOENN}")
    private String pokemonRegion;

    @Bean
    public BattleService battleService(RabbitTemplate rabbitTemplate,
                                       BattleEngine battleEngine,
                                       StadiumService stadiumService) {

        PokemonRegion region = PokemonRegion.valueOf(pokemonRegion);
        String instanceId = region.name().toLowerCase();

        BattleService battleService = new BattleService(rabbitTemplate, battleEngine, instanceId, region);
        stadiumService.registerBattleService(instanceId, battleService);

        System.out.println("🎯 BattleService criado para região: " + region.getName());
        System.out.println("📬 Instance ID: " + instanceId);

        return battleService;
    }
}