package com.gateway.microservice_gateway;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomRouteRefreshListener {

    @Autowired
    private GatewayReadinessHealthIndicator healthIndicator;

    @EventListener
    public void onRoutesRefreshed(RefreshRoutesEvent event) {
        healthIndicator.markRoutesAsLoaded();
    }
}