package com.meetingroom.booking.client.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingReminderRequest {
    private Long bookingId;
    private String meetingTitle;
    private String employeeName;
    private String employeeEmail;
    private String roomName;
    private String startTime;
    private String endTime;
}
