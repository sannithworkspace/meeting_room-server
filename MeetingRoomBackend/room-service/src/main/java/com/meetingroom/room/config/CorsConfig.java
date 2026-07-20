package com.meetingroom.room.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS is centrally handled by API Gateway (port 8080).
 * Downstream microservices do not add CORS headers to prevent duplicate header responses.
 */
@Configuration
public class CorsConfig {
}
