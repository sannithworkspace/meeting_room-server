package com.meetingroom.auth.client;

import com.meetingroom.auth.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", path = "/users")
public interface UserServiceClient {

    @GetMapping("/email/{email}")
    ApiResponse<UserResponse> getUserByEmail(@PathVariable("email") String email);

    @PostMapping("/register")
    ApiResponse<UserResponse> registerUser(@RequestBody Object registerRequest);

    @PostMapping("/generate-reset-otp")
    ApiResponse<String> generateResetOtp(@org.springframework.web.bind.annotation.RequestParam("email") String email);

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(
            @org.springframework.web.bind.annotation.RequestParam("email") String email,
            @org.springframework.web.bind.annotation.RequestParam("otp") String otp,
            @org.springframework.web.bind.annotation.RequestParam("encodedPassword") String encodedPassword
    );
}
