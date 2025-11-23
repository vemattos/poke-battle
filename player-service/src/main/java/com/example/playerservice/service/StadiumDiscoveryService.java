package com.example.playerservice.service;

import com.example.playerservice.dto.Stadium;
import com.example.playerservice.dto.StadiumStatusDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StadiumDiscoveryService {

    private final RestTemplate restTemplate;

    @Value("${stadium.service.instances:http://localhost:8081,http://localhost:8082,http://localhost:8083}")
    private List<String> stadiumInstances;

    private final Map<String, Stadium> availableStadiums = new ConcurrentHashMap<>();

    public StadiumDiscoveryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Stadium> discoverAvailableStadiums() {
        List<Stadium> stadiums = new ArrayList<>();

        for (String instanceUrl : stadiumInstances) {
            try {
                String statusUrl = instanceUrl + "/api/stadium/status";
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        statusUrl, HttpMethod.GET, null,
                        new ParameterizedTypeReference<Map<String, Object>>() {
                        });

                if (response.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> status = response.getBody();
                    String instanceId = (String) status.get("instanceId");
                    String regionName = (String) status.get("regionName");
                    Integer waitingPlayers = (Integer) status.get("waitingPlayers");
                    Integer activeBattles = (Integer) status.get("activeBattles");

                    Stadium stadium = new Stadium();
                    stadium.setInstanceId(instanceId);
                    stadium.setName(regionName != null ? regionName : "Stadium-" + instanceId);
                    stadium.setQueueName("battle.request.queue.stadium-" + instanceId);
                    stadium.setWaitingPlayers(waitingPlayers != null ? waitingPlayers : 0);
                    stadium.setActiveBattles(activeBattles != null ? activeBattles : 0);
                    stadium.setInstanceUrl(instanceUrl);

                    stadiums.add(stadium);
                    availableStadiums.put(instanceId, stadium);

                    System.out.println("Stadium descoberto: " + regionName +
                            " (Jogadores: " + waitingPlayers +
                            ", Batalhas: " + activeBattles + ")");
                }
            } catch (Exception e) {
                System.out.println("Instância indisponível: " + instanceUrl);
            }
        }

        return stadiums;
    }

    public Stadium getStadiumByName(String name) {
        refreshStadiums();

        return discoverAvailableStadiums().stream()
                .filter(s -> s.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

  public StadiumStatusDTO getStadiumStatus(String name) {
        Stadium stadium = getStadiumByName(name);
        String statusUrl = stadium.getInstanceUrl() + "/api/stadium/status";

        ResponseEntity<StadiumStatusDTO> response = restTemplate.exchange(
                statusUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<StadiumStatusDTO>() {}
        );

        return response.getBody();
    }

    public void refreshStadiums() {
        discoverAvailableStadiums();
    }
}