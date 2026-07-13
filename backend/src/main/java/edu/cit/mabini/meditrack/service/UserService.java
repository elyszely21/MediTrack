package edu.cit.mabini.meditrack.user;

import edu.cit.mabini.meditrack.patient.Patient;

import edu.cit.mabini.meditrack.patient.PatientService;

import edu.cit.mabini.meditrack.common.audit.AuditLogService;

import edu.cit.mabini.meditrack.auth.RegisterRequest;
import edu.cit.mabini.meditrack.user.User;
import edu.cit.mabini.meditrack.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Staff accounts only now (SUPER_ADMIN / NURSE / DOCTOR). Patients are a
// separate account type entirely — see PatientService.registerSelf() —
// so this service never creates or touches a Patient row.
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public User register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (request.getRole() == null || request.getRole().isBlank()
                || "PATIENT".equalsIgnoreCase(request.getRole())) {
            // Safety net: this method must never be used to create a
            // patient login — that always goes through
            // PatientService.registerSelf() instead.
            throw new IllegalArgumentException(
                "Staff accounts must be created with an explicit staff role"
            );
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .phoneNumber(request.getPhoneNumber().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);

        auditLogService.log(
            "CREATED",
            "User",
            String.valueOf(saved.getId()),
            "Staff account created: " + saved.getFullName()
                + " — Role: " + saved.getRole()
        );

        return saved;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    // ── Get all nurses ────────────────────────────────────────────────────────

    public List<User> getAllNurses() {
        return userRepository.findByRole("NURSE");
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