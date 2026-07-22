package com.meetingroom.booking.mapper;

import com.meetingroom.booking.dto.request.BookingCreateRequest;
import com.meetingroom.booking.dto.response.BookingResponse;
import com.meetingroom.booking.entity.MeetingBooking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "roomName", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    MeetingBooking toEntity(BookingCreateRequest request);

    BookingResponse toResponse(MeetingBooking entity);
}
