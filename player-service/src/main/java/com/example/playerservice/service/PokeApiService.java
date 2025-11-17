package com.example.playerservice.service;

import com.example.playerservice.dto.PokeApiResponse;
import com.example.playerservice.model.Pokemon;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PokeApiService {

    private static final String POKEAPI_BASE_URL = "https://pokeapi.co/api/v2/pokemon/";
    private final RestTemplate restTemplate;

    public PokeApiService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public PokeApiResponse getPokemonFromApi(String name) {
        try {
            String url = POKEAPI_BASE_URL + name.toLowerCase();
            ResponseEntity<PokeApiResponse> response = restTemplate.getForEntity(url, PokeApiResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            throw new RuntimeException("Pokémon não encontrado na PokeAPI: " + name);

        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Pokémon não encontrado: " + name);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar Pokémon na PokeAPI: " + e.getMessage());
        }
    }

    public Pokemon convertToPokemonEntity(PokeApiResponse apiResponse) {
        Pokemon pokemon = new Pokemon();
        pokemon.setName(apiResponse.getName());
        List<String> types = apiResponse.getTypes();

        pokemon.setType1(types.size() > 0 ? types.get(0) : null);
        pokemon.setType2(types.size() > 1 ? types.get(1) : null);
        pokemon.setHp(apiResponse.getHp());
        pokemon.setAttack(apiResponse.getAttack());
        pokemon.setDefense(apiResponse.getDefense());
        pokemon.setBackSprite(apiResponse.getSprites().getBackDefault());
        pokemon.setFrontSprite(apiResponse.getSprites().getFrontDefault());
        return pokemon;
    }
}