package com.meetingroom.auth.service;

import com.meetingroom.auth.dto.request.LoginRequest;
import com.meetingroom.auth.dto.response.JwtAuthResponse;

import java.util.Map;

public interface AuthService {

    JwtAuthResponse login(LoginRequest request);

    Map<String, Object> validateToken(String token);

    void forgotPassword(String email);

    void resetPassword(String email, String otp, String newPassword);

    void resendActivationOtp(String email);
}
