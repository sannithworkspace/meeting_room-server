package com.meetingroom.room.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "tbl_meeting_room",
    indexes = {
        @Index(name = "idx_room_name", columnList = "room_name"),
        @Index(name = "idx_floor_number", columnList = "floor_number"),
        @Index(name = "idx_capacity", columnList = "seating_capacity")
    }
)
@SQLDelete(sql = "UPDATE tbl_meeting_room SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingRoom extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "seating_capacity", nullable = false)
    private Integer seatingCapacity;

    @ElementCollection(targetClass = Facility.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "tbl_room_facility", joinColumns = @JoinColumn(name = "room_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "facility_name", nullable = false)
    @Builder.Default
    private Set<Facility> availableFacilities = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tbl_room_image", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private Set<String> imageUrls = new HashSet<>();

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;
}
