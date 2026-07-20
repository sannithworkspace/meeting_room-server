package com.meetingroom.booking.exception;

import org.springframework.http.HttpStatus;

public class BookingCollisionException extends BusinessException {

    public BookingCollisionException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
