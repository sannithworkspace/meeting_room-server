package com.meetingroom.user.client;

import com.meetingroom.user.client.request.OtpRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", path = "/notifications")
public interface NotificationClient {

    @PostMapping("/otp")
    ResponseEntity<Map<String, Object>> sendOtp(@RequestBody OtpRequest request);
}
