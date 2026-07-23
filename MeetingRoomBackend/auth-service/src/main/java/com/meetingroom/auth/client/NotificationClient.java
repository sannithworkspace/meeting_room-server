package com.meetingroom.auth.client;

import com.meetingroom.auth.dto.request.OtpRequest;
import com.meetingroom.auth.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/notifications")
public interface NotificationClient {

    @PostMapping("/otp")
    ApiResponse<Void> sendOtp(@RequestBody OtpRequest request);
}
