package com.example.lending.config;

import com.example.lending.entity.User;
import com.example.lending.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        // Drop outdated check constraints created by hibernate update defaults
        try {
            jdbcTemplate.execute("ALTER TABLE loans DROP CONSTRAINT IF EXISTS loans_interest_type_check;");
            log.info("Database constraint loans_interest_type_check dropped successfully if existed.");
        } catch (Exception e) {
            log.warn("Could not drop constraint: {}", e.getMessage());
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created successfully (admin/admin123)");
        } else {
            log.info("Admin user already exists in database");
        }

        // Assign any existing borrowers with null user_id to the admin user
        try {
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin != null) {
                jdbcTemplate.update("UPDATE borrowers SET user_id = ? WHERE user_id IS NULL", admin.getId());
                log.info("Assigned existing ownerless borrowers to the admin user.");
            }
        } catch (Exception e) {
            log.warn("Could not update ownerless borrowers: {}", e.getMessage());
        }
    }
}
