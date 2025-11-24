package com.example.stadiumservice.service;

import com.example.stadiumservice.config.RabbitMQConfig;
import com.example.stadiumservice.dto.ElectionMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ElectionService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQConfig rabbitMQConfig;

    private volatile boolean isLeader = false;
    private volatile String currentLeader = null;
    private volatile String currentInstanceId = null;
    private volatile String currentRegion = null;
    private volatile LocalDateTime lastLeaderHeartbeat = LocalDateTime.now();

    private final Map<String, LocalDateTime> knownInstances = new ConcurrentHashMap<>();
    private final Set<String> activeInstances = ConcurrentHashMap.newKeySet();

    public ElectionService(RabbitTemplate rabbitTemplate,
                           RabbitMQConfig rabbitMQConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMQConfig = rabbitMQConfig;

        System.out.println("🗳ElectionService inicializado (sem StadiumService)");
    }

    public void initialize(String instanceId, String region) {
        this.currentInstanceId = instanceId;
        this.currentRegion = region;

        knownInstances.put(instanceId, LocalDateTime.now());
        activeInstances.add(instanceId);

        System.out.println("ElectionService configurado para: " + instanceId);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
                startElection();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void startElection() {
        if (currentInstanceId == null) return;

        System.out.println("🗳INICIANDO ELEIÇÃO...");

        Optional<String> newLeader = activeInstances.stream()
                .min(String::compareTo);

        if (newLeader.isPresent()) {
            String leaderId = newLeader.get();

            if (!leaderId.equals(currentLeader)) {
                currentLeader = leaderId;
                isLeader = leaderId.equals(currentInstanceId);

                if (isLeader) {
                    System.out.println("ELEITO COMO LÍDER: " + currentInstanceId);
                    announceLeadership();
                } else {
                    System.out.println("NOVO LÍDER ELEITO: " + leaderId);
                }
            }
        }

        lastLeaderHeartbeat = LocalDateTime.now();
    }

    private void announceLeadership() {
        ElectionMessage message = new ElectionMessage(
                currentInstanceId,
                currentRegion,
                ElectionMessage.ElectionMessageType.LEADER_ANNOUNCEMENT
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.ELECTION_EXCHANGE, "", message);
        System.out.println("Anunciando liderança: " + currentInstanceId);
    }

    @Scheduled(fixedRate = 3000)
    public void sendHeartbeat() {
        if (currentInstanceId == null) return;

        ElectionMessage heartbeat = new ElectionMessage(
                currentInstanceId,
                currentRegion,
                ElectionMessage.ElectionMessageType.HEARTBEAT
        );

        rabbitTemplate.convertAndSend(RabbitMQConfig.ELECTION_EXCHANGE, "", heartbeat);

        if (isLeader) {
            System.out.println("Heartbeat do LÍDER enviado: " + currentInstanceId);
        }
    }

    @Scheduled(fixedRate = 10000)
    public void checkLeaderHealth() {
        if (isLeader || currentInstanceId == null) return;

        if (lastLeaderHeartbeat.isBefore(LocalDateTime.now().minusSeconds(15))) {
            System.out.println("Líder inativo detectado: " + currentLeader);
            currentLeader = null;
            startElection();
        }
    }

    public void processElectionMessage(ElectionMessage message) {
        String senderId = message.getInstanceId();
        knownInstances.put(senderId, LocalDateTime.now());
        activeInstances.add(senderId);

        System.out.println("Mensagem de " + senderId + ": " + message.getType());

        switch (message.getType()) {
            case LEADER_ANNOUNCEMENT:
                handleLeaderAnnouncement(message);
                break;

            case HEARTBEAT:
                handleHeartbeat(message);
                break;

            default:
                break;
        }

        cleanupInactiveInstances();
    }

    private void handleLeaderAnnouncement(ElectionMessage message) {
        String newLeader = message.getInstanceId();

        if (!newLeader.equals(currentLeader)) {
            currentLeader = newLeader;
            isLeader = newLeader.equals(currentInstanceId);
            lastLeaderHeartbeat = LocalDateTime.now();

            if (isLeader) {
                System.out.println("RECONHECIDO COMO LÍDER: " + currentInstanceId);
            } else {
                System.out.println("LÍDER RECONHECIDO: " + newLeader);
            }
        }
    }

    private void handleHeartbeat(ElectionMessage message) {
        String senderId = message.getInstanceId();

        if (senderId.equals(currentLeader)) {
            lastLeaderHeartbeat = LocalDateTime.now();
            System.out.println("Heartbeat do líder recebido: " + senderId);
        }
    }

    private void cleanupInactiveInstances() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(30);

        activeInstances.removeIf(instanceId -> {
            LocalDateTime lastSeen = knownInstances.get(instanceId);
            boolean inactive = lastSeen != null && lastSeen.isBefore(cutoff);

            if (inactive && !instanceId.equals(currentInstanceId)) {
                System.out.println("Removendo instância inativa: " + instanceId);

                if (instanceId.equals(currentLeader)) {
                    System.out.println("Líder removido: " + instanceId);
                    currentLeader = null;
                    new Thread(this::startElection).start();
                }
            }

            return inactive;
        });
    }

    public void discoverInstances(List<String> instances) {
        instances.forEach(instance -> {
            if (!instance.equals(currentInstanceId) && !knownInstances.containsKey(instance)) {
                knownInstances.put(instance, LocalDateTime.now().minusMinutes(1));
                activeInstances.add(instance);
                System.out.println("Instância descoberta: " + instance);
            }
        });
    }

    // Getters
    public boolean isLeader() { return isLeader; }
    public String getCurrentLeader() { return currentLeader; }
    public String getCurrentInstanceId() { return currentInstanceId; }

    public void forceElection() {
        System.out.println("Eleição forçada solicitada");
        startElection();
    }
}