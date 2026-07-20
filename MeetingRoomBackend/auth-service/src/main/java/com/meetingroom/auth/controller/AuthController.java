package com.meetingroom.auth.controller;

import com.meetingroom.auth.dto.request.LoginRequest;
import com.meetingroom.auth.dto.response.ApiResponse;
import com.meetingroom.auth.dto.response.JwtAuthResponse;
import com.meetingroom.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RefreshScope
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "Endpoints for employee & admin login and JWT token validation")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and issue JWT token")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to authenticate user: {}", request.getEmail());
        JwtAuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Authentication successful"));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token signature and return claims")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @RequestHeader(name = "Authorization", required = false) String authHeader
    ) {
        log.info("REST request to validate JWT token");
        Map<String, Object> validation = authService.validateToken(authHeader);
        return ResponseEntity.ok(ApiResponse.success(validation, "Token validation completed"));
    }
}
