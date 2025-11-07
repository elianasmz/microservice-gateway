package com.gateway.microservice_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import com.gateway.microservice_gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            // 🧩 Endpoints públicos (no requieren autenticación)
            if (path.contains("/auth/login") ||
                    path.contains("/auth/register") ||
                    path.contains("/auth/validate")) {
                log.debug("🟢 Ruta pública detectada: {}", path);
                return chain.filter(exchange);
            }

            // 1. Verificar si existe el header Authorization
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header");
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            // 2. Extraer el token
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format");
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7).trim();

            // 3. Validar el token
            try {
                if (!jwtUtil.validateToken(token)) {
                    log.warn("Invalid or expired token");
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // 4. Extraer información del token y agregarla a los headers
                String username = jwtUtil.extractUsername(token);
                String roles = jwtUtil.extractRoles(token);

                log.info("✅ Request authenticated - User: {}, Roles: {}", username, roles);

                // 5. Agregar headers personalizados
                exchange = exchange.mutate()
                        .request(r -> r.headers(headers -> {
                            headers.add("X-User-Email", username);
                            headers.add("X-User-Roles", roles);
                        }))
                        .build();

                return chain.filter(exchange);

            } catch (Exception e) {
                log.error("❌ Error validating token: {}", e.getMessage());
                return onError(exchange, "Error validating token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Método para manejar errores y retornar respuestas JSON
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorJson = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                java.time.LocalDateTime.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                exchange.getRequest().getPath()
        );

        byte[] bytes = errorJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }

    public static class Config {
        // Configuración personalizada si es necesario
    }
}