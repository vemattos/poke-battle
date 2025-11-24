package com.example.stadiumservice.config;

import com.example.stadiumservice.dto.PokemonRegion;
import com.example.stadiumservice.service.BattleEngine;
import com.example.stadiumservice.service.BattleService;
import com.example.stadiumservice.service.ElectionService;
import com.example.stadiumservice.service.StadiumService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BattleServicesConfig {

    @Value("${pokemon.region:HOENN}")
    private String pokemonRegion;

    private final RabbitAdmin rabbitAdmin;
    private final RabbitMQConfig rabbitMQConfig;

    public BattleServicesConfig(RabbitAdmin rabbitAdmin, RabbitMQConfig rabbitMQConfig) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    @Bean
    public BattleService battleService(RabbitTemplate rabbitTemplate,
                                       BattleEngine battleEngine,
                                       StadiumService stadiumService,
                                       ElectionService electionService) {

        PokemonRegion region = PokemonRegion.valueOf(pokemonRegion);
        String instanceId = region.name().toLowerCase();

        createElectionQueue(instanceId);

        BattleService battleService = new BattleService(rabbitTemplate, battleEngine, instanceId, region);
        stadiumService.registerBattleService(instanceId, battleService);

        electionService.initialize(instanceId, battleService.getRegionName());

        System.out.println("BattleService criado para região: " + region.getName());
        System.out.println("Instance ID: " + instanceId);

        return battleService;
    }

    private void createElectionQueue(String instanceId) {
        try {
            Queue electionQueue = rabbitMQConfig.createElectionQueue(instanceId);
            rabbitAdmin.declareQueue(electionQueue);

            FanoutExchange electionExchange = rabbitMQConfig.electionExchange();
            Binding binding = BindingBuilder.bind(electionQueue).to(electionExchange);
            rabbitAdmin.declareBinding(binding);

            System.out.println("Fila de eleição criada e vinculada: " + electionQueue.getName());
        } catch (Exception e) {
            System.err.println("Erro ao criar fila de eleição: " + e.getMessage());
        }
    }
}