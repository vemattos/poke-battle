package com.example.stadiumservice.config;

import com.example.stadiumservice.dto.Stadium;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Stadium stadium() {
        return Stadium.getRandom();
    }
}
