package com.gateway.microservice_gateway.config;

import com.gateway.microservice_gateway.filter.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


//@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        log.info("SecurityConfig inicializado correctamente con JwtAuthenticationFilter");
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Configurando SecurityWebFilterChain...");

        log.debug("Rutas públicas permitidas: /api/v1/auth/**, /eureka/**, /swagger-ui.html, /v3/api-docs/**");
        log.debug("Registrando JwtAuthenticationFilter en orden AUTHENTICATION...");

        SecurityWebFilterChain securityChain = http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> {
                    log.trace("⚙️ Configurando reglas de autorización...");
                    exchange
                            .pathMatchers(
                                    "/auth/**",
                                    "/eureka/**",
                                    "/swagger-ui.html",
                                    "/v3/api-docs/**",
                                    "/actuator/**"

                            ).permitAll()
                            .anyExchange().authenticated();
                })
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();

        log.info("SecurityWebFilterChain configurado exitosamente.");
        return securityChain;
    }
}
