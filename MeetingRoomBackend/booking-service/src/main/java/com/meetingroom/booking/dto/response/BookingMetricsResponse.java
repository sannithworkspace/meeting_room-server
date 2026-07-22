package com.meetingroom.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingMetricsResponse {
    private long totalBookings;
    private long upcomingBookings;
    private long ongoingBookings;
    private long completedBookings;
    private long cancelledBookings;
    private Map<String, Long> roomUtilization;
}
