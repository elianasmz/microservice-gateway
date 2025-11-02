package com.gateway.microservice_gateway;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class GatewayReadinessHealthIndicator implements HealthIndicator {

    private volatile boolean routesLoaded = false;

    public void markRoutesAsLoaded() {
        this.routesLoaded = true;
    }

    @Override
    public Health health() {
        if (routesLoaded) {
            return Health.up().withDetail("gateway", "Rutas cargadas").build();
        } else {
            return Health.down().withDetail("gateway", "Rutas no cargadas").build();
        }
    }
}