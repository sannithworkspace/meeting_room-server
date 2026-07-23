package com.meetingroom.booking.scheduler;

import com.meetingroom.booking.client.NotificationClient;
import com.meetingroom.booking.client.request.MeetingReminderRequest;
import com.meetingroom.booking.entity.MeetingBooking;
import com.meetingroom.booking.repository.MeetingBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingReminderScheduler {

    private final MeetingBookingRepository bookingRepository;
    private final NotificationClient notificationClient;

    /**
     * Runs every 10 minutes to verify if there are any meetings starting in the next hour
     * that haven't received a reminder notification yet.
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void sendUpcomingMeetingReminders() {
        log.info("Scheduled task: Checking for upcoming meetings starting in the next hour...");
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime oneHourFromNow = now.plusHours(1);

        List<MeetingBooking> pendingReminders = bookingRepository.findPendingReminders(today, now, oneHourFromNow);
        
        if (pendingReminders.isEmpty()) {
            log.info("No upcoming meetings found requiring reminders.");
            return;
        }

        log.info("Found {} meetings starting between {} and {} requiring reminders.", pendingReminders.size(), now, oneHourFromNow);

        for (MeetingBooking booking : pendingReminders) {
            try {
                // Call notification-service Feign Client to send reminder mail
                notificationClient.sendReminder(MeetingReminderRequest.builder()
                        .bookingId(booking.getId())
                        .meetingTitle(booking.getMeetingTitle())
                        .employeeName(booking.getEmployeeName())
                        .employeeEmail(booking.getEmployeeEmail())
                        .roomName(booking.getRoomName())
                        .startTime(booking.getStartTime().toString())
                        .endTime(booking.getEndTime().toString())
                        .build());
                
                // Mark reminder as sent
                booking.setReminderSent(true);
                bookingRepository.save(booking);
                log.info("Successfully sent 1-hour reminder for Booking ID: {}", booking.getId());
            } catch (Exception ex) {
                log.error("Failed to send 1-hour meeting reminder for Booking ID: {}", booking.getId(), ex);
            }
        }
    }
}
