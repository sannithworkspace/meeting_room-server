package com.meetingroom.room.service.impl;

import com.meetingroom.room.constants.RoomConstants;
import com.meetingroom.room.dto.request.RoomCreateRequest;
import com.meetingroom.room.dto.request.RoomUpdateRequest;
import com.meetingroom.room.dto.response.PageResponse;
import com.meetingroom.room.dto.response.RoomResponse;
import com.meetingroom.room.entity.Facility;
import com.meetingroom.room.entity.MeetingRoom;
import com.meetingroom.room.exception.BusinessException;
import com.meetingroom.room.exception.ResourceNotFoundException;
import com.meetingroom.room.mapper.RoomMapper;
import com.meetingroom.room.repository.MeetingRoomRepository;
import com.meetingroom.room.service.RoomService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final MeetingRoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional
    public RoomResponse createRoom(RoomCreateRequest request) {
        log.info("Creating meeting room with name: '{}' on floor: {}", request.getRoomName(), request.getFloorNumber());

        if (roomRepository.existsByRoomNameAndFloorNumber(request.getRoomName(), request.getFloorNumber())) {
            String errorMsg = String.format(RoomConstants.ROOM_NAME_EXISTS, request.getRoomName(), request.getFloorNumber());
            log.warn("Room creation failed: {}", errorMsg);
            throw new BusinessException(errorMsg);
        }

        MeetingRoom room = roomMapper.toEntity(request);
        MeetingRoom savedRoom = roomRepository.save(room);

        log.info("Meeting room created successfully with ID: {}", savedRoom.getId());
        return roomMapper.toResponse(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, RoomUpdateRequest request) {
        log.info("Updating meeting room ID: {}", id);

        MeetingRoom room = findRoomById(id);

        if (roomRepository.existsByRoomNameAndFloorNumberAndIdNot(request.getRoomName(), request.getFloorNumber(), id)) {
            String errorMsg = String.format(RoomConstants.ROOM_NAME_EXISTS, request.getRoomName(), request.getFloorNumber());
            log.warn("Room update failed for ID {}: {}", id, errorMsg);
            throw new BusinessException(errorMsg);
        }

        roomMapper.updateEntityFromRequest(request, room);
        MeetingRoom updatedRoom = roomRepository.save(room);

        log.info("Meeting room ID: {} updated successfully", id);
        return roomMapper.toResponse(updatedRoom);
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        log.debug("Fetching meeting room details for ID: {}", id);
        MeetingRoom room = findRoomById(id);
        return roomMapper.toResponse(room);
    }

    @Override
    public PageResponse<RoomResponse> getAllRooms(Pageable pageable) {
        log.debug("Fetching all meeting rooms page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<MeetingRoom> roomPage = roomRepository.findAll(pageable);
        Page<RoomResponse> responsePage = roomPage.map(roomMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    public PageResponse<RoomResponse> searchRooms(
            String roomName,
            Integer floorNumber,
            Integer minCapacity,
            Set<Facility> facilities,
            Pageable pageable
    ) {
        log.debug("Searching meeting rooms with filters - roomName: '{}', floor: {}, minCapacity: {}, facilities: {}",
                roomName, floorNumber, minCapacity, facilities);

        Specification<MeetingRoom> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (roomName != null && !roomName.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("roomName")), "%" + roomName.trim().toLowerCase() + "%"));
            }

            if (floorNumber != null) {
                predicates.add(cb.equal(root.get("floorNumber"), floorNumber));
            }

            if (minCapacity != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("seatingCapacity"), minCapacity));
            }

            if (facilities != null && !facilities.isEmpty()) {
                for (Facility facility : facilities) {
                    predicates.add(cb.isMember(facility, root.get("availableFacilities")));
                }
            }

            predicates.add(cb.equal(root.get("isDeleted"), false));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<MeetingRoom> roomPage = roomRepository.findAll(spec, pageable);
        Page<RoomResponse> responsePage = roomPage.map(roomMapper::toResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        log.info("Deleting meeting room with ID: {}", id);
        MeetingRoom room = findRoomById(id);
        roomRepository.delete(room);
        log.info("Meeting room ID: {} marked as deleted (soft delete)", id);
    }

    private MeetingRoom findRoomById(Long id) {
        return roomRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(RoomConstants.ROOM_NOT_FOUND, id)));
    }
}
