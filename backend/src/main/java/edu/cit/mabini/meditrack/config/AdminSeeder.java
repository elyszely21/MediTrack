package edu.cit.mabini.meditrack.config;

import edu.cit.mabini.meditrack.entity.User;
import edu.cit.mabini.meditrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Override
    public void run(String... args) {
        if (userRepository.countByRole("SUPER_ADMIN") > 0) {
            log.info("SUPER_ADMIN already exists, skipping seed.");
            return;
        }

        User admin = User.builder()
                .fullName(adminFullName)
                .email(adminEmail.trim().toLowerCase())
                .phoneNumber("0000000000")
                .password(passwordEncoder.encode(adminPassword))
                .role("SUPER_ADMIN")
                .build();

        userRepository.save(admin);
        log.info("Seeded default SUPER_ADMIN account: {}", adminEmail);
    }
}