package com.example.playerservice.service;

import com.example.playerservice.config.RabbitMQConfig;
import com.example.playerservice.dto.BattleMessage;
import com.example.playerservice.dto.PokemonDTO;
import com.example.playerservice.dto.Stadium;
import com.example.playerservice.dto.UserDTO;
import com.example.playerservice.model.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class BattlePublisher {

    private final RabbitTemplate rabbitTemplate;

    public BattlePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendLoginMessage(User user, Stadium stadium) {
        UserDTO userDTO = convertToDTO(user);

        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.LOGIN);
        message.setUser(userDTO);
        message.setStadium(stadium);

        String queueName = getStadiumQueueName(stadium);
        rabbitTemplate.convertAndSend(queueName, message);
        System.out.println("    Login enviado para " + stadium.getName() + " (" + queueName + "): " + user.getName());
    }

    public void sendBattleAction(BattleMessage message) {
        Stadium stadium = message.getStadium();
        String queueName = getStadiumQueueName(stadium);

        rabbitTemplate.convertAndSend(queueName, message);
        System.out.println("    Ação enviada para " + stadium.getName() + ": " + message.getType() + " - " + message.getUser().getName());
    }

    private String getStadiumQueueName(Stadium stadium) {
        return switch (stadium) {
            case STADIUM_1 -> RabbitMQConfig.BATTLE_REQUEST_QUEUE_1;
            case STADIUM_2 -> RabbitMQConfig.BATTLE_REQUEST_QUEUE_2;
            case STADIUM_3 -> RabbitMQConfig.BATTLE_REQUEST_QUEUE_3;
        };
    }


    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());

        userDTO.setTeam(user.getTeam().stream()
                .map(pokemon -> {
                    PokemonDTO pokemonDTO = new PokemonDTO();
                    pokemonDTO.setId(pokemon.getId());
                    pokemonDTO.setName(pokemon.getName());
                    pokemonDTO.setType1(pokemon.getType1());
                    pokemonDTO.setType2(pokemon.getType2());
                    pokemonDTO.setHp(pokemon.getHp());
                    pokemonDTO.setAttack(pokemon.getAttack());
                    pokemonDTO.setDefense(pokemon.getDefense());
                    pokemonDTO.setCurrentHp(pokemon.getHp());
                    return pokemonDTO;
                })
                .toList());

        return userDTO;
    }
}
