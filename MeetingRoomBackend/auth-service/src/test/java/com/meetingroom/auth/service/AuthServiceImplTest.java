package com.meetingroom.auth.service;

import com.meetingroom.auth.client.UserResponse;
import com.meetingroom.auth.client.UserServiceClient;
import com.meetingroom.auth.dto.request.LoginRequest;
import com.meetingroom.auth.dto.response.ApiResponse;
import com.meetingroom.auth.dto.response.JwtAuthResponse;
import com.meetingroom.auth.exception.BusinessException;
import com.meetingroom.auth.security.JwtTokenProvider;
import com.meetingroom.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        loginRequest = LoginRequest.builder()
                .email("admin@meetingroom.com")
                .password("SuperAdmin123!")
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .fullName("System Super Admin")
                .email("admin@meetingroom.com")
                .password("encodedSuperAdminPassword")
                .roles(Set.of("ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_EMPLOYEE"))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully authenticate valid credentials and return JWT token")
    void login_Success() {
        when(userServiceClient.getUserByEmail("admin@meetingroom.com")).thenReturn(ApiResponse.success(userResponse, "Success"));
        when(passwordEncoder.matches("SuperAdmin123!", "encodedSuperAdminPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(1L, "admin@meetingroom.com", Set.of("ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_EMPLOYEE")))
                .thenReturn("mocked.jwt.token");

        JwtAuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("admin@meetingroom.com", response.getEmail());
    }

    @Test
    @DisplayName("Should throw BusinessException when password mismatch occurs")
    void login_InvalidPassword_ThrowsException() {
        when(userServiceClient.getUserByEmail("admin@meetingroom.com")).thenReturn(ApiResponse.success(userResponse, "Success"));
        when(passwordEncoder.matches("WrongPassword", "encodedSuperAdminPassword")).thenReturn(false);

        loginRequest.setPassword("WrongPassword");

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));
    }
}
