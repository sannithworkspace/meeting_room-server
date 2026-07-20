package com.meetingroom.booking.service;

import com.meetingroom.booking.client.RoomClientResponse;
import com.meetingroom.booking.dto.request.BookingCreateRequest;
import com.meetingroom.booking.dto.response.BookingResponse;
import com.meetingroom.booking.dto.response.PageResponse;
import com.meetingroom.booking.entity.BookingStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingCreateRequest request);

    BookingResponse getBookingById(Long id);

    BookingResponse cancelBooking(Long id);

    PageResponse<BookingResponse> getEmployeeUpcomingBookings(String employeeName, Pageable pageable);

    PageResponse<BookingResponse> searchBookings(
            String meetingTitle,
            String employeeName,
            String roomName,
            Long bookingId,
            BookingStatus status,
            LocalDate bookingDate,
            Pageable pageable
    );

    List<RoomClientResponse> getAvailableRooms(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer requiredCapacity
    );

    void updateBookingStatusesAutomatically();
}
