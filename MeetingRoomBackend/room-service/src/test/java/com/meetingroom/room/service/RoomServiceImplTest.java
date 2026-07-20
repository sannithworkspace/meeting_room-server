package com.meetingroom.room.service;

import com.meetingroom.room.dto.request.RoomCreateRequest;
import com.meetingroom.room.dto.response.RoomResponse;
import com.meetingroom.room.entity.Facility;
import com.meetingroom.room.entity.MeetingRoom;
import com.meetingroom.room.exception.BusinessException;
import com.meetingroom.room.exception.ResourceNotFoundException;
import com.meetingroom.room.mapper.RoomMapper;
import com.meetingroom.room.repository.MeetingRoomRepository;
import com.meetingroom.room.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private MeetingRoomRepository roomRepository;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    private RoomCreateRequest createRequest;
    private MeetingRoom meetingRoom;
    private RoomResponse roomResponse;

    @BeforeEach
    void setUp() {
        createRequest = RoomCreateRequest.builder()
                .roomName("Executive Boardroom")
                .floorNumber(3)
                .seatingCapacity(16)
                .availableFacilities(Set.of(Facility.PROJECTOR, Facility.VIDEO_CONFERENCE, Facility.AIR_CONDITIONING))
                .imageUrls(Set.of("https://images.example.com/room1.jpg"))
                .build();

        meetingRoom = MeetingRoom.builder()
                .id(1L)
                .roomName("Executive Boardroom")
                .floorNumber(3)
                .seatingCapacity(16)
                .availableFacilities(Set.of(Facility.PROJECTOR, Facility.VIDEO_CONFERENCE, Facility.AIR_CONDITIONING))
                .imageUrls(Set.of("https://images.example.com/room1.jpg"))
                .isDeleted(false)
                .build();

        roomResponse = RoomResponse.builder()
                .id(1L)
                .roomName("Executive Boardroom")
                .floorNumber(3)
                .seatingCapacity(16)
                .availableFacilities(Set.of(Facility.PROJECTOR, Facility.VIDEO_CONFERENCE, Facility.AIR_CONDITIONING))
                .imageUrls(Set.of("https://images.example.com/room1.jpg"))
                .build();
    }

    @Test
    @DisplayName("Should successfully create a meeting room when request is valid")
    void createRoom_Success() {
        when(roomRepository.existsByRoomNameAndFloorNumber("Executive Boardroom", 3)).thenReturn(false);
        when(roomMapper.toEntity(createRequest)).thenReturn(meetingRoom);
        when(roomRepository.save(meetingRoom)).thenReturn(meetingRoom);
        when(roomMapper.toResponse(meetingRoom)).thenReturn(roomResponse);

        RoomResponse result = roomService.createRoom(createRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Executive Boardroom", result.getRoomName());
        verify(roomRepository).save(any(MeetingRoom.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when room with same name exists on floor")
    void createRoom_DuplicateName_ThrowsException() {
        when(roomRepository.existsByRoomNameAndFloorNumber("Executive Boardroom", 3)).thenReturn(true);

        assertThrows(BusinessException.class, () -> roomService.createRoom(createRequest));
        verify(roomRepository, never()).save(any(MeetingRoom.class));
    }

    @Test
    @DisplayName("Should return room response when room exists by ID")
    void getRoomById_Success() {
        when(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(meetingRoom));
        when(roomMapper.toResponse(meetingRoom)).thenReturn(roomResponse);

        RoomResponse result = roomService.getRoomById(1L);

        assertNotNull(result);
        assertEquals("Executive Boardroom", result.getRoomName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when room does not exist")
    void getRoomById_NotFound_ThrowsException() {
        when(roomRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoomById(99L));
    }

    @Test
    @DisplayName("Should soft delete meeting room by ID")
    void deleteRoom_Success() {
        when(roomRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(meetingRoom));

        roomService.deleteRoom(1L);

        verify(roomRepository).delete(meetingRoom);
    }
}
