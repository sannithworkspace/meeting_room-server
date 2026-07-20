package com.meetingroom.gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class SwaggerAggregatorConfig {

    private final DiscoveryClient discoveryClient;
    private final SwaggerUiConfigParameters swaggerUiConfigParameters;

    public SwaggerAggregatorConfig(DiscoveryClient discoveryClient, SwaggerUiConfigParameters swaggerUiConfigParameters) {
        this.discoveryClient = discoveryClient;
        this.swaggerUiConfigParameters = swaggerUiConfigParameters;
    }

    @PostConstruct
    public void refreshSwaggerServices() {
        List<String> services = discoveryClient.getServices();
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();

        for (String serviceName : services) {
            // Ignore API-GATEWAY itself and EUREKA-SERVER in swagger specs dropdown
            if (!serviceName.equalsIgnoreCase("api-gateway") && !serviceName.equalsIgnoreCase("eureka-server")) {
                String name = serviceName.toLowerCase();
                String url = "/" + name + "/v3/api-docs";
                urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(serviceName, url, serviceName));
            }
        }
        swaggerUiConfigParameters.setUrls(urls);
    }
}
