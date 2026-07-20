package com.meetingroom.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    @Bean
    @RefreshScope
    public SecretKey jwtSecretKey(
            @Value("${app.jwt.secret:MeetingRoomProjectSuperSecretSigningKey2026With256BitsMinimumLength!}")
            String jwtSecret
    ) {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
