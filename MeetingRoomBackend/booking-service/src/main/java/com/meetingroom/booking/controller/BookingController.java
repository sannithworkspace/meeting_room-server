package com.meetingroom.booking.controller;

import com.meetingroom.booking.client.RoomClientResponse;
import com.meetingroom.booking.constants.ApiConstants;
import com.meetingroom.booking.constants.BookingConstants;
import com.meetingroom.booking.dto.request.BookingCreateRequest;
import com.meetingroom.booking.dto.response.ApiResponse;
import com.meetingroom.booking.dto.response.BookingResponse;
import com.meetingroom.booking.dto.response.BookingMetricsResponse;
import com.meetingroom.booking.dto.response.PageResponse;
import com.meetingroom.booking.entity.BookingStatus;
import com.meetingroom.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RefreshScope
@RestController
@RequestMapping(ApiConstants.BOOKINGS_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Meeting Booking Management", description = "Endpoints for reserving meeting rooms, collision checks, cancellations, and available room discovery")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Reserve a meeting room with collision prevention")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        log.info("REST request to create booking for room ID: {} on {}", request.getRoomId(), request.getBookingDate());
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, BookingConstants.BOOKING_CREATED_SUCCESS));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        log.info("REST request to get booking ID: {}", id);
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(response, BookingConstants.BOOKING_FETCHED_SUCCESS));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an upcoming booking and immediately free the room slot")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        log.info("REST request to cancel booking ID: {} with reason: {}", id, reason);
        BookingResponse response = bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(ApiResponse.success(response, BookingConstants.BOOKING_CANCELLED_SUCCESS));
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get bookings utilization metrics and status counts for dashboard")
    public ResponseEntity<ApiResponse<BookingMetricsResponse>> getBookingMetrics() {
        log.info("REST request to get dashboard booking metrics");
        BookingMetricsResponse response = bookingService.getBookingMetrics();
        return ResponseEntity.ok(ApiResponse.success(response, "Metrics fetched successfully"));
    }

    @GetMapping("/employee/{employeeName}/upcoming")
    @Operation(summary = "View upcoming reservations for a specific employee")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getEmployeeUpcomingBookings(
            @PathVariable String employeeName,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        log.info("REST request to get upcoming bookings for employee: {}", employeeName);
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").ascending().and(Sort.by("startTime").ascending()));
        PageResponse<BookingResponse> response = bookingService.getEmployeeUpcomingBookings(employeeName, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, BookingConstants.BOOKINGS_FETCHED_SUCCESS));
    }

    @GetMapping("/search")
    @Operation(summary = "Search & filter booking history by title, employee, room, status, or date")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> searchBookings(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String employee,
            @RequestParam(required = false) String room,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        log.info("REST request to search bookings - title: {}, employee: {}, room: {}, status: {}", title, employee, room, status);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<BookingResponse> response = bookingService.searchBookings(title, employee, room, bookingId, status, date, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, BookingConstants.BOOKINGS_FETCHED_SUCCESS));
    }

    @GetMapping("/available-rooms")
    @Operation(summary = "Find available rooms with zero collision for a requested date and time slot")
    public ResponseEntity<ApiResponse<List<RoomClientResponse>>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) Integer requiredCapacity
    ) {
        log.info("REST request to search available rooms for date: {} between {} and {}", date, startTime, endTime);
        List<RoomClientResponse> availableRooms = bookingService.getAvailableRooms(date, startTime, endTime, requiredCapacity);
        return ResponseEntity.ok(ApiResponse.success(availableRooms, "Available rooms fetched successfully"));
    }
}
