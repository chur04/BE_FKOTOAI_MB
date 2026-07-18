package com.g5.fokotoai.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Getter
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiConfig {

    @Value("${ai.api-key}")
    String apiKey;

    @Value("${ai.base-url}")
    String baseUrl;

    @Value("${ai.model}")
    String model;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String buildGenerateContentUrl() {
        return baseUrl + "/" + model + ":generateContent?key=" + apiKey;
    }
}
