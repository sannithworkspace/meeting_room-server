package com.meetingroom.auth.service.impl;

import com.meetingroom.auth.client.NotificationClient;
import com.meetingroom.auth.client.UserResponse;
import com.meetingroom.auth.client.UserServiceClient;
import com.meetingroom.auth.dto.request.LoginRequest;
import com.meetingroom.auth.dto.response.ApiResponse;
import com.meetingroom.auth.dto.response.JwtAuthResponse;
import com.meetingroom.auth.exception.BusinessException;
import com.meetingroom.auth.security.JwtTokenProvider;
import com.meetingroom.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RefreshScope
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserServiceClient userServiceClient;
    private final NotificationClient notificationClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public JwtAuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getEmail());

        UserResponse user = fetchUserByEmail(request.getEmail());

        if (user.getIsActive() == null || !user.getIsActive()) {
            log.warn("Login failed: Account for email '{}' is deactivated", request.getEmail());
            throw new BusinessException("User account is deactivated. Please contact support.", HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid credentials for email '{}'", request.getEmail());
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRoles());
        log.info("Login successful for user: {}. Roles: {}", request.getEmail(), user.getRoles());

        return JwtAuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    @Override
    public Map<String, Object> validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        boolean isValid = jwtTokenProvider.validateToken(token);
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        if (isValid) {
            response.put("email", jwtTokenProvider.getEmailFromToken(token));
            response.put("userId", jwtTokenProvider.getUserIdFromToken(token));
            response.put("roles", jwtTokenProvider.getRolesFromToken(token));
        }

        return response;
    }

    private UserResponse fetchUserByEmail(String email) {
        try {
            ApiResponse<UserResponse> response = userServiceClient.getUserByEmail(email);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception ex) {
            log.error("Failed to fetch user details for email: {}", email, ex);
        }
        throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Processing forgot password request for email: {}", email);
        UserResponse user = findUserByEmail(email);

        // Generate reset OTP from user-service
        ApiResponse<String> otpResponse = userServiceClient.generateResetOtp(email);
        if (otpResponse == null || !otpResponse.isSuccess() || otpResponse.getData() == null) {
            throw new BusinessException("Failed to generate password reset verification code", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String otp = otpResponse.getData();

        // Send OTP email via notification-service
        com.meetingroom.auth.dto.request.OtpRequest otpRequest = com.meetingroom.auth.dto.request.OtpRequest.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .otp(otp)
                .type("PASSWORD_RESET")
                .build();
        
        try {
            notificationClient.sendOtp(otpRequest);
            log.info("Forgot password OTP notification request sent successfully for user: {}", email);
        } catch (Exception ex) {
            log.error("Failed to invoke notification-service for forgot password OTP mail", ex);
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        log.info("Processing reset password request for email: {}", email);
        
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new BusinessException("Password must be at least 6 characters long", HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        ApiResponse<Void> resetResponse = userServiceClient.resetPassword(email, otp, encodedPassword);
        if (resetResponse == null || !resetResponse.isSuccess()) {
            String msg = (resetResponse != null) ? resetResponse.getMessage() : "Password reset failed";
            throw new BusinessException(msg, HttpStatus.BAD_REQUEST);
        }
        log.info("Password reset completed successfully in auth-service for user: {}", email);
    }

    private UserResponse findUserByEmail(String email) {
        try {
            ApiResponse<UserResponse> response = userServiceClient.getUserByEmail(email);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception ex) {
            log.error("Failed to find user details for email: {}", email, ex);
        }
        throw new BusinessException("User not found with email: " + email, HttpStatus.NOT_FOUND);
    }

    @Override
    public void resendActivationOtp(String email) {
        log.info("Processing resend activation OTP request for email: {}", email);
        UserResponse user = findUserByEmail(email);

        if (user.getIsActive() != null && user.getIsActive()) {
            throw new BusinessException("Account is already active. Please sign in.", HttpStatus.BAD_REQUEST);
        }

        // Generate new OTP code using the existing user-service OTP generation endpoint
        ApiResponse<String> otpResponse = userServiceClient.generateResetOtp(email);
        if (otpResponse == null || !otpResponse.isSuccess() || otpResponse.getData() == null) {
            throw new BusinessException("Failed to generate account verification code", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String otp = otpResponse.getData();

        // Send OTP email with "ACTIVATION" type via notification-service
        com.meetingroom.auth.dto.request.OtpRequest otpRequest = com.meetingroom.auth.dto.request.OtpRequest.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .otp(otp)
                .type("ACTIVATION")
                .build();
        
        try {
            notificationClient.sendOtp(otpRequest);
            log.info("Resent account activation OTP notification successfully for user: {}", email);
        } catch (Exception ex) {
            log.error("Failed to invoke notification-service for resending activation OTP mail", ex);
        }
    }
}
