package com.example.playerservice.service;

import com.example.playerservice.config.RabbitMQConfig;
import com.example.playerservice.dto.BattleMessage;
import com.example.playerservice.dto.PokemonDTO;
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

    public void sendLoginMessage(User user) {
        UserDTO userDTO = convertToDTO(user);

        BattleMessage message = new BattleMessage();
        message.setType(BattleMessage.MessageType.LOGIN);
        message.setUser(userDTO);

        rabbitTemplate.convertAndSend("battle.request.queue", message);
        System.out.println("[PLAYER] Login request enviado para battle.request.queue: " + user.getName());
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

    public void sendBattleAction(BattleMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.BATTLE_REQUEST_QUEUE, message);
        System.out.println("[PLAYER] Battle action enviado: " + message.getType() + " - " + message.getUser().getName());
    }
}
