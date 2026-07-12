package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.RegisterRequest;
import edu.cit.mabini.meditrack.entity.User;
import edu.cit.mabini.meditrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phoneNumber(request.getPhoneNumber().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() == null || request.getRole().isBlank()
                    ? "NURSE" : request.getRole())
                .build();

        User saved = userRepository.save(user);

        auditLogService.log(
            "CREATED",
            "User",
            String.valueOf(saved.getId()),
            "User registered: " + saved.getFullName()
                + " — Role: " + saved.getRole()
        );

        return saved;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    // ── Get all nurses ────────────────────────────────────────────────────────

    public List<User> getAllNurses() {
        return userRepository.findByRole("ROLE_NURSE");
    }

    // ── Delete user ───────────────────────────────────────────────────────────

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        auditLogService.log(
            "DELETED",
            "User",
            String.valueOf(id),
            "User removed: " + user.getFullName()
                + " — Role: " + user.getRole()
        );

        userRepository.deleteById(id);
    }
}