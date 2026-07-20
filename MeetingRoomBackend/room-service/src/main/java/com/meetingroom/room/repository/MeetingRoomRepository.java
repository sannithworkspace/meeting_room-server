package com.meetingroom.room.repository;

import com.meetingroom.room.entity.MeetingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoom, Long>, JpaSpecificationExecutor<MeetingRoom> {

    boolean existsByRoomNameAndFloorNumber(String roomName, Integer floorNumber);

    boolean existsByRoomNameAndFloorNumberAndIdNot(String roomName, Integer floorNumber, Long id);

    Optional<MeetingRoom> findByIdAndIsDeletedFalse(Long id);
}
