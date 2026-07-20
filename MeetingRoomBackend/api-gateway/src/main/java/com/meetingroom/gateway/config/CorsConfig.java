package com.meetingroom.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS is centrally configured via spring.cloud.gateway.globalcors in api-gateway.yml
 * combined with DeduplicateResponseHeader filter to prevent header duplication.
 */
@Configuration
public class CorsConfig {
    // Handled by spring.cloud.gateway.globalcors in config-server api-gateway.yml
}
