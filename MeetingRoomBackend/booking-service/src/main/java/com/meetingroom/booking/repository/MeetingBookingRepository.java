package com.meetingroom.booking.repository;

import com.meetingroom.booking.entity.BookingStatus;
import com.meetingroom.booking.entity.MeetingBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingBookingRepository extends JpaRepository<MeetingBooking, Long>, JpaSpecificationExecutor<MeetingBooking> {

    Optional<MeetingBooking> findByIdAndIsDeletedFalse(Long id);

    @Query("""
        SELECT b FROM MeetingBooking b 
        WHERE b.roomId = :roomId 
          AND b.bookingDate = :bookingDate 
          AND b.status != com.meetingroom.booking.entity.BookingStatus.CANCELLED 
          AND b.isDeleted = false 
          AND (b.startTime < :endTime AND b.endTime > :startTime)
    """)
    List<MeetingBooking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
        SELECT DISTINCT b.roomId FROM MeetingBooking b 
        WHERE b.bookingDate = :bookingDate 
          AND b.status != com.meetingroom.booking.entity.BookingStatus.CANCELLED 
          AND b.isDeleted = false 
          AND (b.startTime < :endTime AND b.endTime > :startTime)
    """)
    List<Long> findBookedRoomIdsForTimeSlot(
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    Page<MeetingBooking> findByEmployeeNameIgnoreCaseAndStatusAndBookingDateGreaterThanEqualAndIsDeletedFalse(
            String employeeName,
            BookingStatus status,
            LocalDate bookingDate,
            Pageable pageable
    );

    @Query("""
        SELECT b FROM MeetingBooking b 
        WHERE (LOWER(b.employeeName) LIKE LOWER(CONCAT('%', :identifier, '%')) 
            OR LOWER(b.employeeEmail) LIKE LOWER(CONCAT('%', :identifier, '%')))
          AND b.isDeleted = false
        ORDER BY b.bookingDate DESC, b.startTime DESC
    """)
    Page<MeetingBooking> findByEmployeeIdentifier(
            @Param("identifier") String identifier,
            Pageable pageable
    );

    List<MeetingBooking> findByStatusInAndIsDeletedFalse(List<BookingStatus> statuses);
}
