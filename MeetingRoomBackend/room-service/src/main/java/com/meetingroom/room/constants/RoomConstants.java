package com.meetingroom.room.constants;

public final class RoomConstants {

    private RoomConstants() {
        // Prevent instantiation
    }

    public static final String ROOM_CREATED_SUCCESS = "Meeting room created successfully";
    public static final String ROOM_UPDATED_SUCCESS = "Meeting room updated successfully";
    public static final String ROOM_DELETED_SUCCESS = "Meeting room deleted successfully";
    public static final String ROOM_FETCHED_SUCCESS = "Meeting room details fetched successfully";
    public static final String ROOMS_FETCHED_SUCCESS = "Meeting rooms fetched successfully";
    public static final String ROOM_NOT_FOUND = "Meeting room not found with ID: %d";
    public static final String ROOM_NAME_EXISTS = "Meeting room with name '%s' already exists on floor %d";
}
