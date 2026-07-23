package com.meetingroom.notification.controller;

import com.meetingroom.notification.dto.request.BookingNotificationRequest;
import com.meetingroom.notification.dto.request.MeetingReminderRequest;
import com.meetingroom.notification.dto.request.OtpRequest;
import com.meetingroom.notification.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/otp")
    public ResponseEntity<Map<String, Object>> sendOtp(@Valid @RequestBody OtpRequest request) {
        log.info("Received request to send registration OTP email to: {}", request.getEmail());
        emailService.sendOtpEmail(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "success", true,
                "message", "OTP email dispatch accepted and queued"
        ));
    }

    @PostMapping("/booking")
    public ResponseEntity<Map<String, Object>> sendBooking(@Valid @RequestBody BookingNotificationRequest request) {
        log.info("Received request to send booking notification email to: {}", request.getEmployeeEmail());
        emailService.sendBookingEmail(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "success", true,
                "message", "Booking notification email dispatch accepted and queued"
        ));
    }

    @PostMapping("/reminder")
    public ResponseEntity<Map<String, Object>> sendReminder(@Valid @RequestBody MeetingReminderRequest request) {
        log.info("Received request to send meeting 1-hour reminder email to: {}", request.getEmployeeEmail());
        emailService.sendMeetingReminderEmail(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "success", true,
                "message", "Meeting 1-hour reminder email dispatch accepted and queued"
        ));
    }
}
