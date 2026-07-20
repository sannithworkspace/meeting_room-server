package com.meetingroom.user.mapper;

import com.meetingroom.user.dto.request.AdminCreateRequest;
import com.meetingroom.user.dto.request.UserRegisterRequest;
import com.meetingroom.user.dto.response.UserResponse;
import com.meetingroom.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    UserEntity toEntity(UserRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    UserEntity toEntity(AdminCreateRequest request);

    UserResponse toResponse(UserEntity entity);
}
