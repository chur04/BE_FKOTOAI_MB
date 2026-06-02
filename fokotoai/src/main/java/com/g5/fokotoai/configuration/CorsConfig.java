package com.g5.fokotoai.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*"); // Cho phép mọi đường dẫn web
        config.addAllowedHeader("*"); // Cho phép mọi header
        config.addAllowedMethod("*"); // Cho phép mọi phương thức (GET, POST, OPTIONS...)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Áp dụng cho toàn bộ API
        return new CorsFilter(source);
    }
}