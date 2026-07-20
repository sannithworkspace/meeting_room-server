package com.meetingroom.user.config;

import com.meetingroom.user.entity.Role;
import com.meetingroom.user.entity.UserEntity;
import com.meetingroom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String superAdminEmail = "admin@meetingroom.com";

        if (!userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(superAdminEmail)) {
            log.info("Initializing hardcoded Super Admin account: {}", superAdminEmail);

            UserEntity superAdmin = UserEntity.builder()
                    .fullName("System Super Admin")
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode("SuperAdmin123!"))
                    .department("IT Administration")
                    .roles(Set.of(Role.ROLE_SUPER_ADMIN, Role.ROLE_ADMIN, Role.ROLE_EMPLOYEE))
                    .isActive(true)
                    .isDeleted(false)
                    .build();

            userRepository.save(superAdmin);
            log.info("Super Admin initialized successfully! Email: {}, Default Password: SuperAdmin123!", superAdminEmail);
        } else {
            log.info("Super Admin account already exists: {}", superAdminEmail);
        }
    }
}
