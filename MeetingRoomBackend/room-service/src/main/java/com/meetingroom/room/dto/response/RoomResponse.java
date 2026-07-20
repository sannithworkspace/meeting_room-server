package com.meetingroom.room.dto.response;

import com.meetingroom.room.entity.Facility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private Long id;
    private String roomName;
    private Integer floorNumber;
    private Integer seatingCapacity;
    private Set<Facility> availableFacilities;
    private Set<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
