package com.meetingroom.user.service;

import com.meetingroom.user.dto.request.AdminCreateRequest;
import com.meetingroom.user.dto.request.UserRegisterRequest;
import com.meetingroom.user.dto.response.PageResponse;
import com.meetingroom.user.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse registerUser(UserRegisterRequest request);

    UserResponse createAdminUser(AdminCreateRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    void deleteUser(Long id);

    void verifyOtp(String email, String otp);

    String generateResetOtp(String email);

    void resetPassword(String email, String otp, String encodedPassword);
}
