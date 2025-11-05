package com.gateway.microservice_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Value("${app.jwt.secret}")
    private String secretKey;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * Rutas públicas: cualquier subruta bajo estos paths será accesible sin token
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/",
            "/eureka/",
            "/actuator/",
            "/reservations/"
    );

    /**
     * Reglas de roles: rutas privadas que requieren ciertos roles
     */
    private static final Map<String, List<String>> ROLE_RULES = Map.of(
            "/users/", List.of("OWNER", "ADMIN"),
            "/payments/", List.of("OWNER", "ADMIN")
            //"/reservations/", List.of("OWNER", "CARER") // si quisieras proteger algunas rutas de reservations
    );

    private final WebClient webClient = WebClient.create("http://localhost:8084");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.debug("🌐 Petición entrante: {}", path);

        // ✅ Permitir rutas públicas
        if (isPublicPath(path)) {
            log.debug("Ruta pública detectada: {}", path);
            return chain.filter(exchange);
        }

        // ✅ Verificar header Authorization
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Cabecera 'Authorization' inválida o ausente: {}", authHeader);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        // ✅ Validar token con el microservicio Auth
        return webClient.get()
                .uri("/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.warn("Token inválido según AuthService (status: {})", response.statusCode());
                    return Mono.error(new RuntimeException("Token inválido"));
                })
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    String username = (String) response.get("username");
                    List<String> roles = (List<String>) response.get("roles");
                    log.info("✅ Token válido. Usuario: {} | Roles: {}", username, roles);

                    // ✅ Validar rol según la ruta
                    if (!hasPermission(path, roles)) {
                        log.warn("🚫 Acceso denegado. Usuario '{}' no tiene permisos para '{}'", username, path);
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    // ✅ Reenviar el token al microservicio destino
                    var mutatedRequest = exchange.getRequest().mutate()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> {
                    log.error("Error al validar token con AuthService: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    /**
     * Verifica si la ruta es pública
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Verifica si los roles del usuario cumplen con las reglas del endpoint
     */
    private boolean hasPermission(String path, List<String> userRoles) {
        for (Map.Entry<String, List<String>> entry : ROLE_RULES.entrySet()) {
            String routePrefix = entry.getKey();
            List<String> requiredRoles = entry.getValue();

            if (path.startsWith(routePrefix)) {
                return userRoles.stream().anyMatch(requiredRoles::contains);
            }
        }
        return true; // Si no hay regla específica, se permite acceso
    }
}
