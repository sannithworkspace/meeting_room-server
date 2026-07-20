package com.meetingroom.booking.scheduler;

import com.meetingroom.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStatusScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void scheduleStatusUpdates() {
        log.trace("Executing scheduled booking status transition check");
        bookingService.updateBookingStatusesAutomatically();
    }
}
