package com.meetingroom.gateway.controller;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/user")
    public ResponseEntity<Map<String, Object>> userServiceFallback(ServerWebExchange exchange) {
        return buildFallbackResponse("User Service is currently unavailable or timing out. Please try again later.", exchange);
    }

    @RequestMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback(ServerWebExchange exchange) {
        return buildFallbackResponse("Authentication Service is currently unavailable or timing out. Please try again later.", exchange);
    }

    @RequestMapping("/booking")
    public ResponseEntity<Map<String, Object>> bookingServiceFallback(ServerWebExchange exchange) {
        return buildFallbackResponse("Booking Service is currently unavailable or timing out. Please try again later.", exchange);
    }

    @RequestMapping("/room")
    public ResponseEntity<Map<String, Object>> roomServiceFallback(ServerWebExchange exchange) {
        return buildFallbackResponse("Meeting Room Service is currently unavailable or timing out. Please try again later.", exchange);
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String message, ServerWebExchange exchange) {
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("message", message);
        if (traceId != null) {
            response.put("traceId", traceId);
        }
        response.put("path", exchange.getRequest().getURI().getPath());
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
