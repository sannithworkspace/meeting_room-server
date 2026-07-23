package com.meetingroom.user.controller;

import com.meetingroom.user.dto.request.AdminCreateRequest;
import com.meetingroom.user.dto.request.UserRegisterRequest;
import com.meetingroom.user.dto.response.ApiResponse;
import com.meetingroom.user.dto.response.PageResponse;
import com.meetingroom.user.dto.response.UserResponse;
import com.meetingroom.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RefreshScope
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for employee registration, admin creation, and user profile management")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new Employee account")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        log.info("REST request to register employee user: {}", request.getEmail());
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Employee registered successfully"));
    }

    @PostMapping("/admin")
    @Operation(summary = "Create an Admin account (Super Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> createAdminUser(@Valid @RequestBody AdminCreateRequest request) {
        log.info("REST request to create admin user: {}", request.getEmail());
        UserResponse response = userService.createAdminUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Admin user created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("REST request to fetch user ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User details fetched successfully"));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user details by email (Internal / Auth verification)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        log.info("REST request to fetch user by email: {}", email);
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response, "User details fetched successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all registered users with pagination")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        log.info("REST request to list all users - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Users list fetched successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (soft delete) a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("REST request to delete user ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP code to activate employee account")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        log.info("REST request to verify OTP for email: {}", email);
        userService.verifyOtp(email, otp);
        return ResponseEntity.ok(ApiResponse.success("Account activated successfully"));
    }
}
