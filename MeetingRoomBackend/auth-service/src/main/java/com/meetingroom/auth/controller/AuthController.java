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

    @PostMapping("/forgot-password")
    @Operation(summary = "Generate and send reset password OTP code to user's email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@org.springframework.web.bind.annotation.RequestParam String email) {
        log.info("REST request to generate password reset email for: {}", email);
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success("Reset verification code sent successfully"));
    }


//    these the password reset endpoints
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP verification code")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @org.springframework.web.bind.annotation.RequestParam String email,
            @org.springframework.web.bind.annotation.RequestParam String otp,
            @org.springframework.web.bind.annotation.RequestParam String newPassword
    ) {
        log.info("REST request to reset password for: {}", email);
        authService.resetPassword(email, otp, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    @PostMapping("/resend-activation-otp")
    @Operation(summary = "Resend account activation OTP code to user's email")
    public ResponseEntity<ApiResponse<Void>> resendActivationOtp(@org.springframework.web.bind.annotation.RequestParam String email) {
        log.info("REST request to resend activation OTP for: {}", email);
        authService.resendActivationOtp(email);
        return ResponseEntity.ok(ApiResponse.success("Activation verification code resent successfully"));
    }
}
