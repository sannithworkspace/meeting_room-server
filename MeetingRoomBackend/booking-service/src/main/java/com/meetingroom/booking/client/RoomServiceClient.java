package com.meetingroom.booking.client;

import com.meetingroom.booking.dto.response.ApiResponse;
import com.meetingroom.booking.dto.response.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "room-service", path = "/rooms")
public interface RoomServiceClient {

    @GetMapping("/{id}")
    ApiResponse<RoomClientResponse> getRoomById(@PathVariable("id") Long id);

    @GetMapping
    ApiResponse<PageResponse<RoomClientResponse>> getAllRooms(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size
    );
}
