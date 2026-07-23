package com.meetingroom.notification.service;

import com.meetingroom.notification.dto.request.BookingNotificationRequest;
import com.meetingroom.notification.dto.request.MeetingReminderRequest;
import com.meetingroom.notification.dto.request.OtpRequest;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("emailExecutor")
    public void sendOtpEmail(OtpRequest request) {
        log.info("Starting asynchronous email delivery for OTP verification [{}] to user: {}", request.getType(), request.getEmail());
        
        String subject;
        String title;
        String desc;
        
        if ("PASSWORD_RESET".equalsIgnoreCase(request.getType())) {
            subject = "Reset Your Password - MeetingRoom OTP Code";
            title = "Password Reset Request";
            desc = "We received a request to reset your password on the Meeting Room Booking Platform. Please use the verification code below to complete the reset:";
        } else {
            subject = "Verify Your Account - MeetingRoom OTP Code";
            title = "Account Verification";
            desc = "Thank you for signing up on the Meeting Room Booking Platform. Please use the verification code below to complete your registration:";
        }
        
        String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; background-color: #ffffff;'>" +
                "  <h2 style='color: #0f172a; text-align: center; margin-bottom: 24px;'>%s</h2>" +
                "  <p style='font-size: 16px; color: #334155;'>Hello <strong>%s</strong>,</p>" +
                "  <p style='font-size: 15px; color: #334155; line-height: 1.5;'>%s</p>" +
                "  <div style='text-align: center; margin: 30px 0;'>" +
                "    <span style='font-size: 32px; font-weight: bold; letter-spacing: 4px; color: #3b82f6; background-color: #eff6ff; padding: 12px 30px; border-radius: 6px; border: 1px dashed #bfdbfe;'>%s</span>" +
                "  </div>" +
                "  <p style='font-size: 13px; color: #64748b;'>This OTP code is valid for 10 minutes. If you did not request this, please ignore this email.</p>" +
                "  <hr style='border: none; border-top: 1px solid #f1f5f9; margin: 24px 0;'/>" +
                "  <p style='font-size: 12px; color: #94a3b8; text-align: center;'>MeetingRoom Microservices Platform &copy; 2026</p>" +
                "</div>",
                title, request.getFullName(), desc, request.getOtp()
        );

        sendHtmlEmail(request.getEmail(), subject, htmlContent);
    }

    @Async("emailExecutor")
    public void sendBookingEmail(BookingNotificationRequest request) {
        log.info("Starting asynchronous email delivery for Booking ID: {} to user: {}", request.getBookingId(), request.getEmployeeEmail());
        
        String subject;
        String title;
        String statusText;
        String color;
        String additionalDetails = "";

        if (request.isCancellation()) {
            subject = "Booking Cancelled - Meeting Room Reservation #" + request.getBookingId();
            title = "Reservation Cancelled";
            statusText = "CANCELLED";
            color = "#ef4444"; // Rose/Red
            if (request.getCancellationReason() != null && !request.getCancellationReason().trim().isEmpty()) {
                additionalDetails = String.format(
                        "  <p style='font-size: 15px; color: #ef4444;'><strong>Reason for Cancellation:</strong> %s</p>",
                        request.getCancellationReason()
                );
            }
        } else {
            subject = "Booking Confirmed - Meeting Room Reservation #" + request.getBookingId();
            title = "Reservation Confirmed";
            statusText = "UPCOMING";
            color = "#10b981"; // Emerald/Green
        }

        String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; background-color: #ffffff;'>" +
                "  <div style='text-align: center; border-bottom: 2px solid #f1f5f9; padding-bottom: 15px; margin-bottom: 24px;'>" +
                "    <h2 style='color: #0f172a; margin: 0;'>%s</h2>" +
                "    <span style='font-size: 12px; font-weight: bold; color: #ffffff; background-color: %s; padding: 4px 12px; border-radius: 20px; display: inline-block; margin-top: 8px;'>%s</span>" +
                "  </div>" +
                "  <p style='font-size: 16px; color: #334155;'>Hello <strong>%s</strong>,</p>" +
                "  <p style='font-size: 15px; color: #334155; line-height: 1.5;'>Below are the details for your meeting room reservation:</p>" +
                "  <table style='width: 100%%; font-size: 14px; border-collapse: collapse; margin: 20px 0;'>" +
                "    <tr style='background-color: #f8fafc;'><td style='padding: 10px; font-weight: bold; color: #475569; width: 140px;'>Reservation ID:</td><td style='padding: 10px; color: #0f172a;'>#%d</td></tr>" +
                "    <tr><td style='padding: 10px; font-weight: bold; color: #475569;'>Meeting Title:</td><td style='padding: 10px; color: #0f172a;'>%s</td></tr>" +
                "    <tr style='background-color: #f8fafc;'><td style='padding: 10px; font-weight: bold; color: #475569;'>Room Name:</td><td style='padding: 10px; color: #0f172a;'>%s</td></tr>" +
                "    <tr><td style='padding: 10px; font-weight: bold; color: #475569;'>Booking Date:</td><td style='padding: 10px; color: #0f172a;'>%s</td></tr>" +
                "    <tr style='background-color: #f8fafc;'><td style='padding: 10px; font-weight: bold; color: #475569;'>Time Slot:</td><td style='padding: 10px; color: #0f172a;'>%s - %s</td></tr>" +
                "  </table>" +
                "  %s" +
                "  <p style='font-size: 14px; color: #334155;'>If you need to make changes or cancel this booking, please do so directly via the platform dashboard before the scheduled start time.</p>" +
                "  <hr style='border: none; border-top: 1px solid #f1f5f9; margin: 24px 0;'/>" +
                "  <p style='font-size: 12px; color: #94a3b8; text-align: center;'>MeetingRoom Microservices Platform &copy; 2026</p>" +
                "</div>",
                title, color, statusText, request.getEmployeeName(), request.getBookingId(),
                request.getMeetingTitle(), request.getRoomName(), request.getBookingDate(),
                request.getStartTime(), request.getEndTime(), additionalDetails
        );

        // Attach iCalendar invite for confirmed bookings only (non-cancellation)
        String icsContent = null;
        if (!request.isCancellation()) {
            icsContent = generateIcsContent(request);
        }

        sendHtmlEmailWithCalendar(request.getEmployeeEmail(), subject, htmlContent, icsContent);
    }

    @Async("emailExecutor")
    public void sendMeetingReminderEmail(MeetingReminderRequest request) {
        log.info("Starting asynchronous email delivery for 1-Hour Reminder [Booking ID: {}] to user: {}", request.getBookingId(), request.getEmployeeEmail());
        String subject = "Upcoming Meeting Reminder - Room " + request.getRoomName();

        String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; background-color: #ffffff;'>" +
                "  <div style='background-color: #eff6ff; border-left: 4px solid #3b82f6; padding: 12px 15px; margin-bottom: 24px;'>" +
                "    <h3 style='color: #1e3a8a; margin: 0;'>Meeting Starts in 1 Hour!</h3>" +
                "  </div>" +
                "  <p style='font-size: 16px; color: #334155;'>Hello <strong>%s</strong>,</p>" +
                "  <p style='font-size: 15px; color: #334155; line-height: 1.5;'>This is a friendly reminder that your upcoming scheduled meeting starts in 1 hour:</p>" +
                "  <table style='width: 100%%; font-size: 14px; border-collapse: collapse; margin: 20px 0;'>" +
                "    <tr style='background-color: #f8fafc;'><td style='padding: 10px; font-weight: bold; color: #475569; width: 140px;'>Meeting Title:</td><td style='padding: 10px; color: #0f172a;'>%s</td></tr>" +
                "    <tr><td style='padding: 10px; font-weight: bold; color: #475569;'>Room Name:</td><td style='padding: 10px; color: #0f172a;'>%s</td></tr>" +
                "    <tr style='background-color: #f8fafc;'><td style='padding: 10px; font-weight: bold; color: #475569;'>Time Slot:</td><td style='padding: 10px; color: #0f172a;'>%s - %s</td></tr>" +
                "  </table>" +
                "  <p style='font-size: 14px; color: #334155;'>Please make sure to arrive on time to prevent booking slot release policies.</p>" +
                "  <hr style='border: none; border-top: 1px solid #f1f5f9; margin: 24px 0;'/>" +
                "  <p style='font-size: 12px; color: #94a3b8; text-align: center;'>MeetingRoom Microservices Platform &copy; 2026</p>" +
                "</div>",
                request.getEmployeeName(), request.getMeetingTitle(), request.getRoomName(),
                request.getStartTime(), request.getEndTime()
        );

        sendHtmlEmail(request.getEmployeeEmail(), subject, htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        sendHtmlEmailWithCalendar(toEmail, subject, htmlContent, null);
    }

    private void sendHtmlEmailWithCalendar(String toEmail, String subject, String htmlContent, String icsContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (icsContent != null && !icsContent.trim().isEmpty()) {
                helper.addAttachment(
                        "invite.ics", 
                        new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)), 
                        "text/calendar"
                );
            }
            
            mailSender.send(message);
            log.info("Email sent successfully to {} (Calendar attachment: {})", toEmail, icsContent != null);
        } catch (Exception ex) {
            log.error("Failed to send email to {} with subject '{}'", toEmail, subject, ex);
        }
    }

    private String generateIcsContent(BookingNotificationRequest request) {
        // Format: bookingDate = YYYY-MM-DD, startTime/endTime = HH:mm or HH:mm:ss
        String dateStr = request.getBookingDate().replace("-", ""); // YYYYMMDD
        
        String startStr = request.getStartTime().replace(":", "");  // HHmmss
        if (startStr.length() == 4) startStr += "00";
        if (startStr.length() > 6) startStr = startStr.substring(0, 6);
        
        String endStr = request.getEndTime().replace(":", "");      // HHmmss
        if (endStr.length() == 4) endStr += "00";
        if (endStr.length() > 6) endStr = endStr.substring(0, 6);

        return "BEGIN:VCALENDAR\r\n" +
               "VERSION:2.0\r\n" +
               "PRODID:-//MeetingRoom//Scheduling//EN\r\n" +
               "BEGIN:VEVENT\r\n" +
               "UID:booking-" + request.getBookingId() + "@meetingroom.com\r\n" +
               "DTSTAMP:" + dateStr + "T000000Z\r\n" +
               "DTSTART:" + dateStr + "T" + startStr + "\r\n" +
               "DTEND:" + dateStr + "T" + endStr + "\r\n" +
               "SUMMARY:" + request.getMeetingTitle() + "\r\n" +
               "DESCRIPTION:Meeting Room reservation in room: " + request.getRoomName() + "\r\n" +
               "LOCATION:" + request.getRoomName() + "\r\n" +
               "END:VEVENT\r\n" +
               "END:VCALENDAR";
    }
}
