package com.meetingroom.room.dto.request;

import com.meetingroom.room.entity.Facility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateRequest {

    @NotBlank(message = "Room name is required")
    @Size(min = 2, max = 100, message = "Room name must be between 2 and 100 characters")
    private String roomName;

    @NotNull(message = "Floor number is required")
    @Positive(message = "Floor number must be a positive number")
    private Integer floorNumber;

    @NotNull(message = "Seating capacity is required")
    @Positive(message = "Seating capacity must be a positive number")
    private Integer seatingCapacity;

    @NotNull(message = "Available facilities list cannot be null")
    private Set<Facility> availableFacilities;

    private Set<String> imageUrls;
}
