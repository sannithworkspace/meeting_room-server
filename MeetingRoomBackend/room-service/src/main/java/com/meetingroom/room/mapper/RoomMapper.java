package com.meetingroom.room.mapper;

import com.meetingroom.room.dto.request.RoomCreateRequest;
import com.meetingroom.room.dto.request.RoomUpdateRequest;
import com.meetingroom.room.dto.response.RoomResponse;
import com.meetingroom.room.entity.MeetingRoom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    MeetingRoom toEntity(RoomCreateRequest request);

    RoomResponse toResponse(MeetingRoom entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntityFromRequest(RoomUpdateRequest request, @MappingTarget MeetingRoom entity);
}
