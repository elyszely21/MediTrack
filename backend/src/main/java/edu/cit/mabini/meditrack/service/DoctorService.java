package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.DoctorDto;
import edu.cit.mabini.meditrack.dto.RegisterDoctorRequest;
import edu.cit.mabini.meditrack.entity.User;
import edu.cit.mabini.meditrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    // ── Get all doctors ───────────────────────────────────────────────────────

    public List<DoctorDto> getAllDoctors() {
        return userRepository.findByRole("ROLE_DOCTOR")
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Register doctor ───────────────────────────────────────────────────────

    public DoctorDto registerDoctor(RegisterDoctorRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User doctor = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_DOCTOR")
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .build();

        User saved = userRepository.save(doctor);

        auditLogService.log(
            "CREATED",
            "Doctor",
            String.valueOf(saved.getId()),
            "Doctor registered: " + saved.getFullName()
                + " — " + saved.getSpecialization()
        );

        return toDto(saved);
    }

    // ── Update doctor ─────────────────────────────────────────────────────────

    public DoctorDto updateDoctor(Long id, RegisterDoctorRequest request) {
        User doctor = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (!doctor.getRole().equals("ROLE_DOCTOR")) {
            throw new IllegalArgumentException("User is not a doctor");
        }

        doctor.setFullName(request.getFullName());
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            doctor.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(doctor);

        auditLogService.log(
            "UPDATED",
            "Doctor",
            String.valueOf(id),
            "Doctor updated: " + saved.getFullName()
        );

        return toDto(saved);
    }

    // ── Delete doctor ─────────────────────────────────────────────────────────

    public void deleteDoctor(Long id) {
        User doctor = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        if (!doctor.getRole().equals("ROLE_DOCTOR")) {
            throw new IllegalArgumentException("User is not a doctor");
        }

        auditLogService.log(
            "DELETED",
            "Doctor",
            String.valueOf(id),
            "Doctor removed: " + doctor.getFullName()
        );

        userRepository.deleteById(id);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private DoctorDto toDto(User u) {
        return DoctorDto.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .specialization(u.getSpecialization())
                .licenseNumber(u.getLicenseNumber())
                .role(u.getRole())
                .createdAt(u.getCreatedAt())
                .build();
    }
}