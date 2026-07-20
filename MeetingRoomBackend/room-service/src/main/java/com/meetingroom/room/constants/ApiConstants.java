package com.meetingroom.room.constants;

public final class ApiConstants {

    private ApiConstants() {
        // Prevent instantiation
    }

    public static final String ROOMS_BASE_PATH = "/rooms";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "roomName";
    public static final String DEFAULT_SORT_DIRECTION = "ASC";
}
