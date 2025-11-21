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
    private final StadiumDiscoveryService stadiumDiscoveryService;

    public BattlePublisher(RabbitTemplate rabbitTemplate, StadiumDiscoveryService stadiumDiscoveryService) {
        this.rabbitTemplate = rabbitTemplate;
        this.stadiumDiscoveryService = stadiumDiscoveryService;
    }

    public void sendLoginMessage(User user) {
        try {
            // Encontrar o melhor estádio disponível
            Stadium optimalStadium = stadiumDiscoveryService.getOptimalStadium();

            UserDTO userDTO = convertToDTO(user);

            BattleMessage message = new BattleMessage();
            message.setType(BattleMessage.MessageType.LOGIN);
            message.setUser(userDTO);
            message.setInstanceId(optimalStadium.getInstanceId()); // ✅ Nova linha

            String queueName = optimalStadium.getQueueName();
            rabbitTemplate.convertAndSend(queueName, message);

            System.out.println("🎯 Login enviado para " + optimalStadium.getName() +
                    " (Instância: " + optimalStadium.getInstanceId() + ")");
            System.out.println("   👤 Jogador: " + user.getName());
            System.out.println("   📊 Estatísticas: " + optimalStadium.getWaitingPlayers() +
                    " esperando, " + optimalStadium.getActiveBattles() + " batalhas ativas");

        } catch (Exception e) {
            System.out.println("❌ Erro ao enviar login: " + e.getMessage());
            throw new RuntimeException("Não foi possível conectar a um stadium service", e);
        }
    }

    public void sendBattleAction(BattleMessage message) {
        try {
            String instanceId = message.getInstanceId();
            if (instanceId == null) {
                throw new RuntimeException("InstanceId não especificado na mensagem");
            }

            String queueName = "battle.request.queue.stadium-" + instanceId;
            rabbitTemplate.convertAndSend(queueName, message);

            System.out.println("🎯 Ação enviada para instância " + instanceId +
                    ": " + message.getType() + " - " + message.getUser().getName() +
                    " | Batalha: " + message.getBattleId());

        } catch (Exception e) {
            System.out.println("❌ Erro ao enviar ação: " + e.getMessage());
            throw new RuntimeException("Não foi possível enviar ação para o stadium service", e);
        }
    }

    // Mantenha o método convertToDTO existente
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