package com.gateway.microservice_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Origenes permitidos
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:8080"
        ));

        // Metodos permitidos
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Headers permitidos
        corsConfig.setAllowedHeaders(List.of("*"));

        // Permitir credenciales
        corsConfig.setAllowCredentials(true);

        // Tiempo de caché de preflight
        corsConfig.setMaxAge(3600L);

        // Headers expuestos
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-User-Email",
                "X-User-Roles"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}