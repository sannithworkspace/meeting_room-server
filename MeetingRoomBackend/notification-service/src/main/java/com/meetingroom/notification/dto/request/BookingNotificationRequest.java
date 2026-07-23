package com.meetingroom.notification.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingNotificationRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Meeting title is required")
    private String meetingTitle;

    @NotBlank(message = "Employee name is required")
    private String employeeName;

    @NotBlank(message = "Employee email is required")
    @Email(message = "Invalid email format")
    private String employeeEmail;

    @NotBlank(message = "Room name is required")
    private String roomName;

    @NotNull(message = "Booking date is required")
    private String bookingDate;

    @NotNull(message = "Start time is required")
    private String startTime;

    @NotNull(message = "End time is required")
    private String endTime;

    @Builder.Default
    private boolean isCancellation = false;

    private String cancellationReason;
}
