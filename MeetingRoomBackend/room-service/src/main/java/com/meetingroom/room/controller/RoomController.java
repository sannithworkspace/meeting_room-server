package com.meetingroom.room.controller;

import com.meetingroom.room.constants.ApiConstants;
import com.meetingroom.room.constants.RoomConstants;
import com.meetingroom.room.dto.request.RoomCreateRequest;
import com.meetingroom.room.dto.request.RoomUpdateRequest;
import com.meetingroom.room.dto.response.ApiResponse;
import com.meetingroom.room.dto.response.PageResponse;
import com.meetingroom.room.dto.response.RoomResponse;
import com.meetingroom.room.entity.Facility;
import com.meetingroom.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping(ApiConstants.ROOMS_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Meeting Room Management", description = "Endpoints for creating, updating, searching, and managing meeting rooms")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "Create a new meeting room")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        log.info("REST request to create meeting room: {}", request.getRoomName());
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, RoomConstants.ROOM_CREATED_SUCCESS));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload image file for meeting room to AWS S3 and save URL in database")
    public ResponseEntity<ApiResponse<RoomResponse>> uploadRoomImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        log.info("REST request to upload image for room ID: {}", id);
        RoomResponse response = roomService.uploadRoomImage(id, file);
        return ResponseEntity.ok(ApiResponse.success(response, "Room image uploaded and saved successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing meeting room")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomUpdateRequest request
    ) {
        log.info("REST request to update meeting room ID: {}", id);
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, RoomConstants.ROOM_UPDATED_SUCCESS));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meeting room details by ID")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        log.info("REST request to fetch meeting room ID: {}", id);
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(response, RoomConstants.ROOM_FETCHED_SUCCESS));
    }

    @GetMapping
    @Operation(summary = "Get all meeting rooms with pagination")
    public ResponseEntity<ApiResponse<PageResponse<RoomResponse>>> getAllRooms(
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        log.info("REST request to get all meeting rooms - page: {}, size: {}", page, size);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<RoomResponse> response = roomService.getAllRooms(pageable);
        return ResponseEntity.ok(ApiResponse.success(response, RoomConstants.ROOMS_FETCHED_SUCCESS));
    }

    @GetMapping("/search")
    @Operation(summary = "Search meeting rooms by facilities, floor, capacity, or name")
    public ResponseEntity<ApiResponse<PageResponse<RoomResponse>>> searchRooms(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Set<Facility> facilities,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        log.info("REST request to search rooms - name: {}, floor: {}, minCapacity: {}", name, floor, minCapacity);
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponse<RoomResponse> response = roomService.searchRooms(name, floor, minCapacity, facilities, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, RoomConstants.ROOMS_FETCHED_SUCCESS));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (soft delete) a meeting room by ID")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        log.info("REST request to delete meeting room ID: {}", id);
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success(RoomConstants.ROOM_DELETED_SUCCESS));
    }
}
