package com.example.stadiumservice.consumer;

import com.example.stadiumservice.config.RabbitMQConfig;
import com.example.stadiumservice.dto.BattleMessage;
import com.example.stadiumservice.service.BattleService;
import com.example.stadiumservice.service.StadiumService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@DependsOn("battleService")
public class BattleConsumer {

    private final StadiumService stadiumService;
    private final RabbitAdmin rabbitAdmin;
    private final RabbitMQConfig rabbitMQConfig;
    private String instanceQueueName;
    private final Map<String, BattleService> instanceBattleServices = new HashMap<>();

    public BattleConsumer(StadiumService stadiumService, RabbitAdmin rabbitAdmin,
                          RabbitMQConfig rabbitMQConfig) {
        this.stadiumService = stadiumService;
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitMQConfig = rabbitMQConfig;
    }

    @PostConstruct
    public void init() {
        // Aguarda o BattleService ser registrado no StadiumService
        try {
            Thread.sleep(1000); // Pequeno delay para garantir registro
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        setupInstanceQueue();
    }

    private void setupInstanceQueue() {
        BattleService currentBattleService = stadiumService.getCurrentInstanceService();
        if (currentBattleService != null) {
            String instanceId = currentBattleService.getInstanceId();
            this.instanceQueueName = RabbitMQConfig.BATTLE_REQUEST_QUEUE_PREFIX + instanceId;

            // Cria a queue dinamicamente para esta instância
            Queue queue = rabbitMQConfig.createBattleRequestQueue(instanceId);
            rabbitAdmin.declareQueue(queue);

            // Registra esta instância no mapa local
            instanceBattleServices.put(instanceId, currentBattleService);

            System.out.println("✅ BattleConsumer configurado para instância: " + instanceId);
            System.out.println("📬 Queue: " + instanceQueueName);
        } else {
            System.out.println("BattleService não encontrado no StadiumService");
        }
    }

    @RabbitListener(queues = "#{@battleService.getInstanceId() != null ? " +
            "T(com.example.stadiumservice.config.RabbitMQConfig).BATTLE_REQUEST_QUEUE_PREFIX + @battleService.getInstanceId() : 'battle.request.queue.default'}")
    public void receiveBattleRequest(BattleMessage message) {
        String instanceId = message.getInstanceId();
        if (instanceId == null) {
            instanceId = stadiumService.getCurrentInstanceId();
        }

        System.out.println("Mensagem recebida na instância " + instanceId +
                ": " + message.getType() + " - " +
                (message.getUser() != null ? message.getUser().getName() : "No User"));

        BattleService battleService = getBattleServiceForMessage(message, instanceId);

        if (battleService == null) {
            System.out.println("BattleService não encontrado para instância: " + instanceId);
            return;
        }

        handleBattleRequest(message, battleService, instanceId);
    }

    private BattleService getBattleServiceForMessage(BattleMessage message, String targetInstanceId) {
        // Se a mensagem especifica uma instância, tenta usar essa
        if (message.getInstanceId() != null && !message.getInstanceId().equals(targetInstanceId)) {
            BattleService targetService = stadiumService.getAllInstanceServices().get(message.getInstanceId());
            if (targetService != null) {
                return targetService;
            }
        }

        // Caso contrário, usa a instância atual
        return stadiumService.getCurrentInstanceService();
    }

    private void handleBattleRequest(BattleMessage message, BattleService battleService, String instanceId) {
        try {
            switch (message.getType()) {
                case LOGIN:
                    if (message.getUser() != null) {
                        battleService.handlePlayerLogin(message.getUser());
                    } else {
                        System.out.println(" User é null na mensagem LOGIN");
                    }
                    break;

                case PLAYER_ACTION:
                    if (message.getUser() != null && message.getBattleId() != null) {
                        battleService.handleBattleAction(message);
                    } else {
                        System.out.println("Dados incompletos na mensagem PLAYER_ACTION");
                    }
                    break;

                case BATTLE_START:
                case TURN_RESULT:
                case BATTLE_END:
                    // Estas mensagens são geralmente de saída, não de entrada
                    System.out.println("Mensagem de tipo " + message.getType() + " recebida no consumer - geralmente é de saída");
                    break;

                default:
                    System.out.println("Tipo de request não reconhecido: " + message.getType());
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar mensagem na instância " + instanceId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para adicionar dinamicamente novas instâncias (útil para clustering)
    public void registerInstanceBattleService(String instanceId, BattleService battleService) {
        instanceBattleServices.put(instanceId, battleService);
        System.out.println("Nova instância registrada no BattleConsumer: " + instanceId);
    }

    public String getCurrentInstanceQueueName() {
        return instanceQueueName;
    }

    public Map<String, BattleService> getInstanceBattleServices() {
        return instanceBattleServices;
    }
}