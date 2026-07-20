package com.meetingroom.booking.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomClientResponse {

    private Long id;
    private String roomName;
    private Integer floorNumber;
    private Integer seatingCapacity;
    private Set<String> availableFacilities;
    private Set<String> imageUrls;
}
