package com.meetingroom.user.service;

import com.meetingroom.user.dto.request.UserRegisterRequest;
import com.meetingroom.user.dto.response.UserResponse;
import com.meetingroom.user.entity.Role;
import com.meetingroom.user.entity.UserEntity;
import com.meetingroom.user.exception.BusinessException;
import com.meetingroom.user.mapper.UserMapper;
import com.meetingroom.user.repository.UserRepository;
import com.meetingroom.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegisterRequest registerRequest;
    private UserEntity userEntity;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        registerRequest = UserRegisterRequest.builder()
                .fullName("Jane Employee")
                .email("jane.employee@company.com")
                .password("Password123!")
                .department("Engineering")
                .build();

        userEntity = UserEntity.builder()
                .id(10L)
                .fullName("Jane Employee")
                .email("jane.employee@company.com")
                .password("encodedPassword123")
                .department("Engineering")
                .roles(Set.of(Role.ROLE_EMPLOYEE))
                .isActive(true)
                .isDeleted(false)
                .build();

        userResponse = UserResponse.builder()
                .id(10L)
                .fullName("Jane Employee")
                .email("jane.employee@company.com")
                .department("Engineering")
                .roles(Set.of(Role.ROLE_EMPLOYEE))
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new employee user")
    void registerUser_Success() {
        when(userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse("jane.employee@company.com")).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(userEntity);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword123");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse response = userService.registerUser(registerRequest);

        assertNotNull(response);
        assertEquals("jane.employee@company.com", response.getEmail());
        assertTrue(response.getRoles().contains(Role.ROLE_EMPLOYEE));
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when registering an already existing email")
    void registerUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse("jane.employee@company.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should fetch user by email successfully")
    void getUserByEmail_Success() {
        when(userRepository.findByEmailIgnoreCaseAndIsDeletedFalse("jane.employee@company.com")).thenReturn(Optional.of(userEntity));
        when(userMapper.toResponse(userEntity)).thenReturn(userResponse);

        UserResponse response = userService.getUserByEmail("jane.employee@company.com");

        assertNotNull(response);
        assertEquals("jane.employee@company.com", response.getEmail());
    }
}
