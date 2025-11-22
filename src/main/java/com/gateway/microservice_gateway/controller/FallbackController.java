package com.gateway.microservice_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authFallback() {
        log.error("Auth service is currently unavailable");
        return buildErrorResponse(
                "Auth Service Unavailable",
                "El servicio de autenticación no está disponible en este momento. Por favor, intente más tarde.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> usersFallback() {
        log.error("Users service is currently unavailable");
        return buildErrorResponse(
                "Users Service Unavailable",
                "El servicio de usuarios no está disponible en este momento. Por favor, intente más tarde.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> rolesFallback() {
        log.error("Roles service is currently unavailable");
        return buildErrorResponse(
                "Roles Service Unavailable",
                "El servicio de roles no está disponible en este momento. Por favor, intente más tarde.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> servicesFallback() {
        log.error("Services microservice is currently unavailable");
        return buildErrorResponse(
                "Services Microservice Unavailable",
                "El microservicio de servicios no está disponible en este momento. Por favor, intente más tarde.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    @GetMapping("/reservations")
    public ResponseEntity<Map<String, Object>> reservationsFallback() {
        log.error("Reservations microservice is currently unavailable");
        return buildErrorResponse(
                "Reservations Microservice Unavailable",
                "El microservicio de reservas no está disponible en este momento. Por favor, intente más tarde.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    @GetMapping("/invoices")
    public ResponseEntity<Map<String, Object>> invoicesFallback() {
        log.error("Invoices microservice is currently unavailable");
        return buildErrorResponse(
                "Invoices Microservice Unavailable",
                "El microservicio de facturas no está disponible en este momento. Por favor, intente más tarde.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    /**
     * Construir respuesta de error estandarizada
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            String error,
            String message,
            HttpStatus status) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);

        return ResponseEntity.status(status).body(response);
    }
}