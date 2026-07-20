package com.meetingroom.booking.service.impl;

import com.meetingroom.booking.client.RoomClientResponse;
import com.meetingroom.booking.client.RoomServiceClient;
import com.meetingroom.booking.constants.BookingConstants;
import com.meetingroom.booking.dto.request.BookingCreateRequest;
import com.meetingroom.booking.dto.response.ApiResponse;
import com.meetingroom.booking.dto.response.BookingResponse;
import com.meetingroom.booking.dto.response.PageResponse;
import com.meetingroom.booking.entity.BookingStatus;
import com.meetingroom.booking.entity.MeetingBooking;
import com.meetingroom.booking.exception.BookingCollisionException;
import com.meetingroom.booking.exception.BusinessException;
import com.meetingroom.booking.exception.ResourceNotFoundException;
import com.meetingroom.booking.mapper.BookingMapper;
import com.meetingroom.booking.repository.MeetingBookingRepository;
import com.meetingroom.booking.service.BookingService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RefreshScope
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final MeetingBookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RoomServiceClient roomServiceClient;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        log.info("Creating meeting booking for room ID: {} on {}", request.getRoomId(), request.getBookingDate());

        validateBookingTimeAndDate(request.getBookingDate(), request.getStartTime(), request.getEndTime());

        // Call room-service via OpenFeign to verify room existence and capacity
        RoomClientResponse room = fetchRoomDetails(request.getRoomId());
        if (room.getSeatingCapacity() < request.getNumberOfParticipants()) {
            String errorMsg = String.format(BookingConstants.INSUFFICIENT_ROOM_CAPACITY, room.getSeatingCapacity(), request.getNumberOfParticipants());
            log.warn("Booking failed: {}", errorMsg);
            throw new BusinessException(errorMsg);
        }

        // Time Collision Check for room and date slot
        List<MeetingBooking> collisions = bookingRepository.findOverlappingBookings(
                request.getRoomId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (!collisions.isEmpty()) {
            String errorMsg = String.format(
                    BookingConstants.TIME_COLLISION_DETECTED,
                    room.getRoomName(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getBookingDate()
            );
            log.warn("Booking collision detected for room ID {}: {}", request.getRoomId(), errorMsg);
            throw new BookingCollisionException(errorMsg);
        }

        MeetingBooking booking = bookingMapper.toEntity(request);
        booking.setRoomName(room.getRoomName());
        booking.setStatus(BookingStatus.UPCOMING);

        MeetingBooking savedBooking = bookingRepository.save(booking);
        log.info("Meeting booking created successfully with ID: {}", savedBooking.getId());
        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        log.debug("Fetching booking details for ID: {}", id);
        MeetingBooking booking = findBookingById(id);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        log.info("Attempting to cancel booking ID: {}", id);
        MeetingBooking booking = findBookingById(id);

        if (booking.getStatus() != BookingStatus.UPCOMING) {
            String errorMsg = String.format(BookingConstants.CANNOT_CANCEL_NON_UPCOMING, booking.getStatus());
            log.warn("Cancellation rejected for booking ID {}: {}", id, errorMsg);
            throw new BusinessException(errorMsg);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        MeetingBooking cancelledBooking = bookingRepository.save(booking);

        log.info("Booking ID: {} cancelled successfully. Room slot freed.", id);
        return bookingMapper.toResponse(cancelledBooking);
    }

    @Override
    public PageResponse<BookingResponse> getEmployeeUpcomingBookings(String employeeName, Pageable pageable) {
        log.debug("Fetching all reservations for employee identifier: '{}'", employeeName);
        Page<MeetingBooking> bookingPage = bookingRepository.findByEmployeeIdentifier(employeeName, pageable);
        return PageResponse.from(bookingPage.map(bookingMapper::toResponse));
    }

    @Override
    public PageResponse<BookingResponse> searchBookings(
            String meetingTitle,
            String employeeName,
            String roomName,
            Long bookingId,
            BookingStatus status,
            LocalDate bookingDate,
            Pageable pageable
    ) {
        log.debug("Searching bookings with filters - title: '{}', employee: '{}', room: '{}', status: {}",
                meetingTitle, employeeName, roomName, status);

        Specification<MeetingBooking> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookingId != null) {
                predicates.add(cb.equal(root.get("id"), bookingId));
            }

            if (meetingTitle != null && !meetingTitle.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("meetingTitle")), "%" + meetingTitle.trim().toLowerCase() + "%"));
            }

            if (employeeName != null && !employeeName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("employeeName")), "%" + employeeName.trim().toLowerCase() + "%"));
            }

            if (roomName != null && !roomName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("roomName")), "%" + roomName.trim().toLowerCase() + "%"));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (bookingDate != null) {
                predicates.add(cb.equal(root.get("bookingDate"), bookingDate));
            }

            predicates.add(cb.equal(root.get("isDeleted"), false));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<MeetingBooking> page = bookingRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(bookingMapper::toResponse));
    }

    @Override
    public List<RoomClientResponse> getAvailableRooms(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer requiredCapacity
    ) {
        log.debug("Finding available rooms for date: {}, time: {} - {}, capacity: {}", date, startTime, endTime, requiredCapacity);

        validateBookingTimeAndDate(date, startTime, endTime);

        // Query booked room IDs for this time slot
        List<Long> bookedRoomIds = bookingRepository.findBookedRoomIdsForTimeSlot(date, startTime, endTime);
        Set<Long> unavailableRoomIds = Set.copyOf(bookedRoomIds);

        // Fetch all rooms from room-service
        ApiResponse<PageResponse<RoomClientResponse>> roomsResponse = roomServiceClient.getAllRooms(0, 500);
        if (roomsResponse == null || roomsResponse.getData() == null || roomsResponse.getData().getContent() == null) {
            return Collections.emptyList();
        }

        List<RoomClientResponse> allRooms = roomsResponse.getData().getContent();

        // Filter rooms with zero collision and required seating capacity
        return allRooms.stream()
                .filter(room -> !unavailableRoomIds.contains(room.getId()))
                .filter(room -> requiredCapacity == null || room.getSeatingCapacity() >= requiredCapacity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateBookingStatusesAutomatically() {
        log.debug("Automated task: Checking and updating meeting booking statuses");
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<MeetingBooking> activeBookings = bookingRepository.findByStatusInAndIsDeletedFalse(
                List.of(BookingStatus.UPCOMING, BookingStatus.ONGOING)
        );

        for (MeetingBooking booking : activeBookings) {
            if (booking.getBookingDate().isBefore(today)) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else if (booking.getBookingDate().isEqual(today)) {
                if (now.isAfter(booking.getEndTime())) {
                    booking.setStatus(BookingStatus.COMPLETED);
                } else if (now.isAfter(booking.getStartTime()) || now.equals(booking.getStartTime())) {
                    booking.setStatus(BookingStatus.ONGOING);
                }
            }
            bookingRepository.save(booking);
        }
    }

    private void validateBookingTimeAndDate(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException(BookingConstants.PAST_DATE_NOT_ALLOWED);
        }

        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(BookingConstants.END_TIME_BEFORE_START_TIME);
        }

        if (startTime.isBefore(BookingConstants.OFFICE_HOURS_START) || endTime.isAfter(BookingConstants.OFFICE_HOURS_END)) {
            throw new BusinessException(BookingConstants.OUTSIDE_OFFICE_HOURS);
        }
    }

    private MeetingBooking findBookingById(Long id) {
        return bookingRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(BookingConstants.BOOKING_NOT_FOUND, id)));
    }

    private RoomClientResponse fetchRoomDetails(Long roomId) {
        try {
            ApiResponse<RoomClientResponse> response = roomServiceClient.getRoomById(roomId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception ex) {
            log.error("Failed to fetch room details from room-service for room ID: {}", roomId, ex);
        }
        throw new ResourceNotFoundException(String.format(BookingConstants.ROOM_NOT_FOUND, roomId));
    }
}
