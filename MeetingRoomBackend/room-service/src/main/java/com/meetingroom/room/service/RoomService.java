package com.meetingroom.room.service;

import com.meetingroom.room.dto.request.RoomCreateRequest;
import com.meetingroom.room.dto.request.RoomUpdateRequest;
import com.meetingroom.room.dto.response.PageResponse;
import com.meetingroom.room.dto.response.RoomResponse;
import com.meetingroom.room.entity.Facility;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface RoomService {

    RoomResponse createRoom(RoomCreateRequest request);

    RoomResponse updateRoom(Long id, RoomUpdateRequest request);

    RoomResponse getRoomById(Long id);

    PageResponse<RoomResponse> getAllRooms(Pageable pageable);

    PageResponse<RoomResponse> searchRooms(
            String roomName,
            Integer floorNumber,
            Integer minCapacity,
            Set<Facility> facilities,
            Pageable pageable
    );

    void deleteRoom(Long id);
}
