package edu.cit.mabini.meditrack.user;

import edu.cit.mabini.meditrack.common.audit.AuditLogService;
import edu.cit.mabini.meditrack.auth.RegisterRequest;
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

    // ── Create Nurse ─────────────────────────────────────────────────────────

    public NurseDto createNurse(RegisterNurseRequest request) {
        // Reuse existing staff creation logic but lock role to NURSE.
        RegisterRequest staffRequest = new RegisterRequest();
        staffRequest.setFullName(request.getFullName());
        staffRequest.setEmail(request.getEmail());
        staffRequest.setPhoneNumber(request.getContactNumber());
        staffRequest.setPassword(request.getPassword());
        staffRequest.setConfirmPassword(request.getConfirmPassword());
        staffRequest.setRole("NURSE");

        User saved = register(staffRequest);

        // Map doctor-specific fields available on User
        saved.setLicenseNumber(request.getLicenseNumber());
        saved.setSpecialization(request.getSpecialization());

        User updated = userRepository.save(saved);

        return toNurseDto(updated);
    }

    // ── Update Nurse ─────────────────────────────────────────────────────────

    public NurseDto updateNurse(Long id, UpdateNurseRequest request) {
        User nurse = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nurse not found"));

        if (!"NURSE".equalsIgnoreCase(nurse.getRole())) {
            throw new IllegalArgumentException("Cannot edit non-nurse user");
        }

        String newEmail = request.getEmail().trim().toLowerCase();
        boolean emailChanged = !nurse.getEmail().equalsIgnoreCase(newEmail);
        if (emailChanged && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }

        nurse.setFullName(request.getFullName().trim());
        nurse.setEmail(newEmail);
        nurse.setPhoneNumber(request.getContactNumber().trim());
        nurse.setLicenseNumber(request.getLicenseNumber());
        nurse.setSpecialization(request.getSpecialization());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match");
            }
            nurse.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(nurse);

        auditLogService.log(
                "UPDATED",
                "Nurse",
                String.valueOf(id),
                "Nurse updated: " + saved.getFullName()
                        + " — Role: " + saved.getRole()
        );

        return toNurseDto(saved);
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

    // ── Mapping ─────────────────────────────────────────────────────────────

    private NurseDto toNurseDto(User u) {
        return NurseDto.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .contactNumber(u.getPhoneNumber())
                .licenseNumber(u.getLicenseNumber())
                .specialization(u.getSpecialization())
                .role(u.getRole())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
