package com.meetingroom.booking.service;

import com.meetingroom.booking.client.RoomClientResponse;
import com.meetingroom.booking.client.RoomServiceClient;
import com.meetingroom.booking.dto.request.BookingCreateRequest;
import com.meetingroom.booking.dto.response.ApiResponse;
import com.meetingroom.booking.dto.response.BookingResponse;
import com.meetingroom.booking.entity.BookingStatus;
import com.meetingroom.booking.entity.MeetingBooking;
import com.meetingroom.booking.exception.BookingCollisionException;
import com.meetingroom.booking.exception.BusinessException;
import com.meetingroom.booking.mapper.BookingMapper;
import com.meetingroom.booking.repository.MeetingBookingRepository;
import com.meetingroom.booking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private MeetingBookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private RoomServiceClient roomServiceClient;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingCreateRequest createRequest;
    private MeetingBooking meetingBooking;
    private BookingResponse bookingResponse;
    private RoomClientResponse roomResponse;

    @BeforeEach
    void setUp() {
        createRequest = BookingCreateRequest.builder()
                .meetingTitle("Q3 Roadmap Discussion")
                .employeeName("John Doe")
                .employeeEmail("john.doe@company.com")
                .roomId(1L)
                .bookingDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .numberOfParticipants(8)
                .meetingDescription("Quarterly Strategy Alignment")
                .build();

        roomResponse = RoomClientResponse.builder()
                .id(1L)
                .roomName("Conference Room Alpha")
                .floorNumber(2)
                .seatingCapacity(12)
                .availableFacilities(Set.of("PROJECTOR"))
                .build();

        meetingBooking = MeetingBooking.builder()
                .id(100L)
                .meetingTitle("Q3 Roadmap Discussion")
                .employeeName("John Doe")
                .employeeEmail("john.doe@company.com")
                .roomId(1L)
                .roomName("Conference Room Alpha")
                .bookingDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .numberOfParticipants(8)
                .status(BookingStatus.UPCOMING)
                .isDeleted(false)
                .build();

        bookingResponse = BookingResponse.builder()
                .id(100L)
                .meetingTitle("Q3 Roadmap Discussion")
                .employeeName("John Doe")
                .roomId(1L)
                .roomName("Conference Room Alpha")
                .bookingDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(BookingStatus.UPCOMING)
                .build();
    }

    @Test
    @DisplayName("Should successfully create a booking when slot is free and within office hours")
    void createBooking_Success() {
        when(roomServiceClient.getRoomById(1L)).thenReturn(ApiResponse.success(roomResponse, "Success"));
        when(bookingRepository.findOverlappingBookings(eq(1L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(Collections.emptyList());
        when(bookingMapper.toEntity(createRequest)).thenReturn(meetingBooking);
        when(bookingRepository.save(meetingBooking)).thenReturn(meetingBooking);
        when(bookingMapper.toResponse(meetingBooking)).thenReturn(bookingResponse);

        BookingResponse response = bookingService.createBooking(createRequest);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(BookingStatus.UPCOMING, response.getStatus());
        verify(bookingRepository).save(any(MeetingBooking.class));
    }

    @Test
    @DisplayName("Should throw BookingCollisionException when another booking overlaps")
    void createBooking_TimeCollision_ThrowsException() {
        when(roomServiceClient.getRoomById(1L)).thenReturn(ApiResponse.success(roomResponse, "Success"));
        when(bookingRepository.findOverlappingBookings(eq(1L), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(List.of(meetingBooking));

        assertThrows(BookingCollisionException.class, () -> bookingService.createBooking(createRequest));
        verify(bookingRepository, never()).save(any(MeetingBooking.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when booking outside office hours (08:00 to 20:00)")
    void createBooking_OutsideOfficeHours_ThrowsException() {
        createRequest.setStartTime(LocalTime.of(7, 0)); // Before 08:00

        assertThrows(BusinessException.class, () -> bookingService.createBooking(createRequest));
        verify(bookingRepository, never()).save(any(MeetingBooking.class));
    }

    @Test
    @DisplayName("Should cancel an upcoming booking and update status to CANCELLED")
    void cancelBooking_Success() {
        when(bookingRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(meetingBooking));
        when(bookingRepository.save(meetingBooking)).thenReturn(meetingBooking);
        when(bookingMapper.toResponse(meetingBooking)).thenReturn(bookingResponse);

        BookingResponse response = bookingService.cancelBooking(100L, "Test cancel reason");

        assertNotNull(response);
        verify(bookingRepository).save(meetingBooking);
        assertEquals(BookingStatus.CANCELLED, meetingBooking.getStatus());
    }

    @Test
    @DisplayName("Should throw BusinessException when attempting to cancel a COMPLETED booking")
    void cancelBooking_NonUpcoming_ThrowsException() {
        meetingBooking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(meetingBooking));

        assertThrows(BusinessException.class, () -> bookingService.cancelBooking(100L, "Test cancel reason"));
    }
}
