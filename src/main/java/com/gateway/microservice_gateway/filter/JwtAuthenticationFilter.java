package com.gateway.microservice_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

    private static final String[] PUBLIC_PATHS = {
            "/auth",
            "/eureka",
            "/actuator"
    };

    private static final Map<String, List<String>> protectedRoutes = Map.of(
            "/apirest-users", List.of("ROLE_ADMIN"),
            "/apirest-payments", List.of("ROLE_OWNER", "ROLE_ADMIN"),
            "/apirest-reservations", List.of("ROLE_OWNER", "ROLE_CARER")
    );

    private final WebClient webClient = WebClient.create("http://localhost:8084");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.debug("🌐 Petición entrante: {}", path);

        // ✅ Permitir rutas públicas
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                log.info("Ruta pública detectada: {} — no se requiere autenticación.", path);
                return chain.filter(exchange);
            }
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
                .onStatus(status -> status.isError(), response -> {
                    log.warn("Token inválido según AuthService (status: {})", response.statusCode());
                    return Mono.error(new RuntimeException("Token inválido"));
                })
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    String username = (String) response.get("username");
                    List<String> roles = (List<String>) response.get("roles");
                    log.info("✅ Token válido. Usuario: {} | Roles: {}", username, roles);

                    // 🔒 Validar roles según la ruta
                    for (Map.Entry<String, List<String>> entry : protectedRoutes.entrySet()) {
                        String routePrefix = entry.getKey();
                        List<String> requiredRoles = entry.getValue();

                        if (path.startsWith(routePrefix)) {
                            boolean hasAccess = roles.stream().anyMatch(requiredRoles::contains);
                            if (!hasAccess) {
                                log.warn("🚫 Acceso denegado. Usuario '{}' no tiene roles requeridos para '{}'", username, path);
                                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                return exchange.getResponse().setComplete();
                            }
                        }
                    }

                    log.debug("➡️ Continuando con el request hacia el microservicio destino...");
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    log.error("Error al validar token con AuthService: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}
