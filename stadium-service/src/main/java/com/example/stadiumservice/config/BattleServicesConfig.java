package com.example.stadiumservice.config;

import com.example.stadiumservice.dto.Stadium;
import com.example.stadiumservice.service.BattleEngine;
import com.example.stadiumservice.service.BattleService;
import com.example.stadiumservice.service.StadiumService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BattleServicesConfig {

    @Bean
    public BattleService battleServiceStadium1(RabbitTemplate rabbitTemplate,
                                               BattleEngine battleEngine,
                                               StadiumService stadiumService) {
        BattleService battleService = new BattleService(rabbitTemplate, battleEngine, Stadium.STADIUM_1);
        stadiumService.registerBattleService(Stadium.STADIUM_1, battleService);
        return battleService;
    }

    @Bean
    public BattleService battleServiceStadium2(RabbitTemplate rabbitTemplate,
                                               BattleEngine battleEngine,
                                               StadiumService stadiumService) {
        BattleService battleService = new BattleService(rabbitTemplate, battleEngine, Stadium.STADIUM_2);
        stadiumService.registerBattleService(Stadium.STADIUM_2, battleService);
        return battleService;
    }

    @Bean
    public BattleService battleServiceStadium3(RabbitTemplate rabbitTemplate,
                                               BattleEngine battleEngine,
                                               StadiumService stadiumService) {
        BattleService battleService = new BattleService(rabbitTemplate, battleEngine, Stadium.STADIUM_3);
        stadiumService.registerBattleService(Stadium.STADIUM_3, battleService);
        return battleService;
    }
}
