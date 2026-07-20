package com.meetingroom.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    /**
     * Use ObjectProvider so we always resolve the current @RefreshScope instance of
     * SecretKey — meaning JWT secret changes from Config Server are picked up
     * after POST /actuator/refresh without restarting the gateway.
     */
    private final ObjectProvider<SecretKey> jwtSecretKeyProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/**",
            "/users/register",
            "/fallback/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/*/v3/api-docs",
            "/actuator/**"
    );

    @Autowired
    public JwtAuthenticationFilter(ObjectProvider<SecretKey> jwtSecretKeyProvider) {
        this.jwtSecretKeyProvider = jwtSecretKeyProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // ── CORS Preflight: always pass OPTIONS through without JWT check ──
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            log.trace("OPTIONS preflight request to {}. Bypassing JWT filter.", path);
            return chain.filter(exchange);
        }

        // Check if path is whitelisted as public
        for (String pattern : PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(pattern, path)) {
                log.trace("Public path matched: {}. Skipping JWT verification.", path);
                return chain.filter(exchange);
            }
        }

        // Check Authorization header presence
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized access attempt to {}: Missing Bearer Authorization header", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            // Resolve the current SecretKey from the @RefreshScope bean
            SecretKey key = jwtSecretKeyProvider.getObject();

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String email = claims.getSubject();
            Object userId = claims.get("userId");
            Object rolesObj = claims.get("roles");

            String rolesStr = rolesObj != null ? String.join(",", (List<String>) rolesObj) : "";

            log.info("JWT Token verified for user '{}' with roles [{}] accessing path: {}", email, rolesStr, path);

            // Mutate request headers to pass authenticated user claims downstream
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Email", email)
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Roles", rolesStr)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception ex) {
            log.warn("JWT Token validation failed for path {}: {}", path, ex.getMessage());
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonPayload = String.format(
                "{\"success\":false,\"status\":%d,\"message\":\"%s\",\"timestamp\":\"%s\"}",
                httpStatus.value(),
                errMessage,
                LocalDateTime.now()
        );

        DataBuffer buffer = response.bufferFactory().wrap(jsonPayload.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // High priority global filter
    }
}
