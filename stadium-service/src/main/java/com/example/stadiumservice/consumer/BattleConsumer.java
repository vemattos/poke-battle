package com.example.stadiumservice.consumer;

import com.example.stadiumservice.config.RabbitMQConfig;
import com.example.stadiumservice.dto.BattleMessage;
import com.example.stadiumservice.dto.Stadium;
import com.example.stadiumservice.service.BattleService;
import com.example.stadiumservice.service.StadiumService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BattleConsumer {

    private final StadiumService stadiumService;

    public BattleConsumer(StadiumService stadiumService) {
        this.stadiumService = stadiumService;
    }

    @RabbitListener(queues = RabbitMQConfig.BATTLE_REQUEST_QUEUE_1)
    public void receiveBattleRequestStadium1(BattleMessage message) {
        handleBattleRequest(message, Stadium.STADIUM_1);
    }

    @RabbitListener(queues = RabbitMQConfig.BATTLE_REQUEST_QUEUE_2)
    public void receiveBattleRequestStadium2(BattleMessage message) {
        handleBattleRequest(message, Stadium.STADIUM_2);
    }

    @RabbitListener(queues = RabbitMQConfig.BATTLE_REQUEST_QUEUE_3)
    public void receiveBattleRequestStadium3(BattleMessage message) {
        handleBattleRequest(message, Stadium.STADIUM_3);
    }

    private void handleBattleRequest(BattleMessage message, Stadium stadium) {
        System.out.println("📨 Mensagem recebida no " + stadium.getName() + ": " + message.getType() + " - " + message.getUser().getName());

        BattleService battleService = stadiumService.getStadiumService(stadium);

        switch (message.getType()) {
            case LOGIN:
                battleService.handlePlayerLogin(message.getUser());
                break;
            case PLAYER_ACTION:
                battleService.handleBattleAction(message);
                break;
            default:
                System.out.println("Tipo de request não reconhecido: " + message.getType());
        }
    }
}