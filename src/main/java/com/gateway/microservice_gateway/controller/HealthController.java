package com.gateway.microservice_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/gateway")
public class HealthController {
    /**
     * Endpoint de salud del API Gateway
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "API Gateway");
        return ResponseEntity.ok(health);
    }

    /**
     * Informacion del Gateway
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "API Gateway - Cuidado de Mascotas");
        info.put("version", "1.0.0");
        info.put("description", "Gateway centralizado con validación JWT y descubrimiento de servicios");
        info.put("timestamp", LocalDateTime.now());

        // Configuracion
        Map<String, Object> config = new HashMap<>();
        config.put("backendServer", "http://localhost:9090");
        config.put("port", 8080);
        info.put("configuration", config);

        return ResponseEntity.ok(info);
    }
}