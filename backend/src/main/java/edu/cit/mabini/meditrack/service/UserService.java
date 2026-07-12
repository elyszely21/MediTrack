package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.RegisterRequest;
import edu.cit.mabini.meditrack.entity.Patient;
import edu.cit.mabini.meditrack.entity.User;
import edu.cit.mabini.meditrack.repository.PatientRepository;
import edu.cit.mabini.meditrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository    userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder   passwordEncoder;
    private final AuditLogService   auditLogService;

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

        // Public self-registration is always PATIENT — give every patient
        // account its own row in the patients table (that's what feeds
        // appointments, prescriptions, medical records, etc.) instead of
        // leaving them as a bare login with nothing clinical attached.
        if ("PATIENT".equalsIgnoreCase(saved.getRole())) {
            createLinkedPatientProfile(saved);
        }

        return saved;
    }

    private void createLinkedPatientProfile(User user) {
        if (patientRepository.existsByUserId(user.getId())) {
            return;
        }

        String fullName = user.getFullName().trim();
        String firstName = fullName;
        String lastName = "-";
        int spaceIdx = fullName.indexOf(' ');
        if (spaceIdx > 0) {
            firstName = fullName.substring(0, spaceIdx).trim();
            String rest = fullName.substring(spaceIdx + 1).trim();
            if (!rest.isBlank()) {
                lastName = rest;
            }
        }

        Patient patient = Patient.builder()
                .user(user)
                .patientNumber(generatePatientNumber(user.getId()))
                .firstName(firstName)
                .lastName(lastName)
                .contactNumber(user.getPhoneNumber())
                .build();

        Patient saved = patientRepository.save(patient);

        auditLogService.log(
            "CREATED",
            "Patient",
            String.valueOf(saved.getId()),
            "Patient profile auto-created for new account: "
                + saved.getFirstName() + " " + saved.getLastName()
                + " (" + saved.getPatientNumber() + ")"
        );
    }

    private String generatePatientNumber(Long userId) {
        return "PT-" + String.format("%05d", userId);
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