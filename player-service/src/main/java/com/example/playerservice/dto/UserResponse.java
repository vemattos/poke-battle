package com.example.playerservice.dto;

import com.example.playerservice.model.Pokemon;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private int id;
    private String name;
    private List<Pokemon> team;
}