package com.meetingroom.booking.constants;

import java.time.LocalTime;

public final class BookingConstants {

    private BookingConstants() {
        // Prevent instantiation
    }

    public static final LocalTime OFFICE_HOURS_START = LocalTime.of(8, 0);
    public static final LocalTime OFFICE_HOURS_END = LocalTime.of(20, 0);

    public static final String BOOKING_CREATED_SUCCESS = "Meeting room reserved successfully";
    public static final String BOOKING_CANCELLED_SUCCESS = "Booking cancelled successfully and room slot freed";
    public static final String BOOKING_FETCHED_SUCCESS = "Booking details fetched successfully";
    public static final String BOOKINGS_FETCHED_SUCCESS = "Bookings list fetched successfully";

    public static final String BOOKING_NOT_FOUND = "Booking not found with ID: %d";
    public static final String ROOM_NOT_FOUND = "Meeting room not found with ID: %d";
    public static final String OUTSIDE_OFFICE_HOURS = "Booking time must be within office hours (08:00 to 20:00)";
    public static final String END_TIME_BEFORE_START_TIME = "End time must be strictly after start time";
    public static final String PAST_DATE_NOT_ALLOWED = "Booking date cannot be in the past";
    public static final String TIME_COLLISION_DETECTED = "Room '%s' is already booked between %s and %s on %s";
    public static final String INSUFFICIENT_ROOM_CAPACITY = "Selected room capacity (%d) is smaller than requested participants (%d)";
    public static final String CANNOT_CANCEL_NON_UPCOMING = "Only UPCOMING bookings can be cancelled. Current status is %s";
}
