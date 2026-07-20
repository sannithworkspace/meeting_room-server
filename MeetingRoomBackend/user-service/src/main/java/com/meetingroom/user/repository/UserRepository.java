package com.meetingroom.user.repository;

import com.meetingroom.user.entity.Role;
import com.meetingroom.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmailIgnoreCaseAndIsDeletedFalse(String email);

    boolean existsByEmailIgnoreCaseAndIsDeletedFalse(String email);

    boolean existsByRolesContaining(Role role);
}
