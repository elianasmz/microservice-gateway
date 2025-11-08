package com.gateway.microservice_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/gateway")
public class HealthController {

    @Autowired
    private DiscoveryClient discoveryClient;

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
     * Listar servicios registrados en Eureka
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> listServices() {
        List<String> services = discoveryClient.getServices();

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("totalServices", services.size());
        response.put("services", services);

        // Detalles de cada servicio
        Map<String, List<ServiceInstance>> serviceDetails = new HashMap<>();
        for (String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            serviceDetails.put(service, instances);
        }
        response.put("details", serviceDetails);

        return ResponseEntity.ok(response);
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
        config.put("eurekaServer", "http://localhost:8761/eureka/");
        config.put("port", 8080);
        info.put("configuration", config);

        return ResponseEntity.ok(info);
    }
}