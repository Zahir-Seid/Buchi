package com.buchi.petfinder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.dogapi")
@Data
public class DogApiProperties {

    private String apiKey;
    private String baseUrl = "https://api.thedogapi.com/v1";
}
