package com.meetingroom.booking.client;

import com.meetingroom.booking.client.request.BookingNotificationRequest;
import com.meetingroom.booking.client.request.MeetingReminderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service", path = "/notifications")
public interface NotificationClient {

    @PostMapping("/booking")
    ResponseEntity<Map<String, Object>> sendBooking(@RequestBody BookingNotificationRequest request);

    @PostMapping("/reminder")
    ResponseEntity<Map<String, Object>> sendReminder(@RequestBody MeetingReminderRequest request);
}
