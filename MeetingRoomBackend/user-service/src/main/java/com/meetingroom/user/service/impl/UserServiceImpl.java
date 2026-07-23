package com.meetingroom.user.service.impl;

import com.meetingroom.user.dto.request.AdminCreateRequest;
import com.meetingroom.user.dto.request.UserRegisterRequest;
import com.meetingroom.user.dto.response.PageResponse;
import com.meetingroom.user.dto.response.UserResponse;
import com.meetingroom.user.entity.Role;
import com.meetingroom.user.entity.UserEntity;
import com.meetingroom.user.exception.BusinessException;
import com.meetingroom.user.exception.ResourceNotFoundException;
import com.meetingroom.user.mapper.UserMapper;
import com.meetingroom.user.repository.UserRepository;
import com.meetingroom.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@RefreshScope
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final com.meetingroom.user.client.NotificationClient notificationClient;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        log.info("Registering new employee user with email: {}", request.getEmail());

        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(request.getEmail())) {
            throw new BusinessException(String.format("User with email '%s' already exists", request.getEmail()));
        }

        UserEntity user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(Role.ROLE_EMPLOYEE));
        user.setIsActive(false); // Inactive until verified by OTP

        // Generate 6-digit verification OTP
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setVerificationOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));

        UserEntity savedUser = userRepository.save(user);
        log.info("Employee registered successfully as INACTIVE with ID: {}. Generated OTP: {}", savedUser.getId(), otp);

        // Async notify using notification-service Feign Client
        try {
            notificationClient.sendOtp(com.meetingroom.user.client.request.OtpRequest.builder()
                    .email(savedUser.getEmail())
                    .fullName(savedUser.getFullName())
                    .otp(otp)
                    .build());
            log.info("Queued OTP email notification request successfully for user: {}", savedUser.getEmail());
        } catch (Exception ex) {
            log.error("Failed to invoke notification-service for user registration OTP mail", ex);
        }

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse createAdminUser(AdminCreateRequest request) {
        log.info("Super Admin creating admin user with email: {}", request.getEmail());

        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(request.getEmail())) {
            throw new BusinessException(String.format("User with email '%s' already exists", request.getEmail()));
        }

        UserEntity user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(request.getRoles());
        user.setIsActive(true);

        UserEntity savedAdmin = userRepository.save(user);
        log.info("Admin user created successfully with ID: {}", savedAdmin.getId());
        return userMapper.toResponse(savedAdmin);
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user details for ID: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User not found with ID: %d", id)));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user details for email: {}", email);
        UserEntity user = userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User not found with email: %s", email)));
        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Fetching all users page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        return PageResponse.from(userPage.map(userMapper::toResponse));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user ID: {}", id);
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User not found with ID: %d", id)));

        if (user.getRoles().contains(Role.ROLE_SUPER_ADMIN)) {
            throw new BusinessException("Super Admin account cannot be deleted!");
        }

        userRepository.delete(user);
        log.info("User ID: {} marked as deleted", id);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp) {
        log.info("Verifying OTP for user email: {}", email);
        UserEntity user = userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User not found with email: %s", email)));

        if (user.getIsActive()) {
            log.info("User account with email '{}' is already active.", email);
            return;
        }

        if (user.getVerificationOtp() == null || !user.getVerificationOtp().equals(otp)) {
            log.warn("OTP verification failed: Invalid OTP code provided for email '{}'", email);
            throw new BusinessException("Invalid verification OTP code.");
        }

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            log.warn("OTP verification failed: OTP code has expired for email '{}'", email);
            throw new BusinessException("Verification OTP code has expired. Please register again.");
        }

        user.setIsActive(true);
        user.setVerificationOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        log.info("User account with email '{}' verified and activated successfully!", email);
    }
}
