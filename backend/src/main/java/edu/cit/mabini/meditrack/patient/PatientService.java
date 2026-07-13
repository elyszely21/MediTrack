package edu.cit.mabini.meditrack.patient;

import edu.cit.mabini.meditrack.common.audit.AuditLogService;

import edu.cit.mabini.meditrack.patient.PatientDto;
import edu.cit.mabini.meditrack.auth.RegisterRequest;
import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import edu.cit.mabini.meditrack.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final AuditLogService   auditLogService;

    // ── Public self-registration ─────────────────────────────────────────────
    // This is what /api/auth/register calls. A registering patient lands
    // directly in the patients table with their own login credentials —
    // never in the staff "users" table.
    public Patient registerSelf(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // Keep emails unique across both account tables so login lookup
        // (which checks users first, then patients) is never ambiguous.
        if (patientRepository.existsByEmail(email) || userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String fullName = request.getFullName().trim();
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

        // patientNumber has a NOT NULL + UNIQUE constraint and we want it
        // derived from the row's own id, so save once with a temporary
        // placeholder, then update it now that an id exists.
        Patient patient = Patient.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .patientNumber("PT-PENDING-" + java.util.UUID.randomUUID().toString().substring(0, 8))
                .firstName(firstName)
                .lastName(lastName)
                .contactNumber(request.getPhoneNumber().trim())
                .build();

        Patient saved = patientRepository.save(patient);
        saved.setPatientNumber(String.format("PT-%05d", saved.getId()));
        saved = patientRepository.save(saved);

        auditLogService.log(
            "CREATED",
            "Patient",
            String.valueOf(saved.getId()),
            "Patient self-registered: " + saved.getFirstName() + " " + saved.getLastName()
                + " (" + saved.getPatientNumber() + ")"
        );

        return saved;
    }

    public List<PatientDto> findAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PatientDto getPatient(Long id) {
        return patientRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
    }

    public PatientDto createPatient(PatientDto dto) {
        if (patientRepository.existsByPatientNumber(dto.getPatientNumber())) {
            throw new IllegalArgumentException("Patient number already exists");
        }

        Patient patient = Patient.builder()
                .patientNumber(dto.getPatientNumber())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .address(dto.getAddress())
                .contactNumber(dto.getContactNumber())
                .emergencyContact(dto.getEmergencyContact())
                .build();

        Patient saved = patientRepository.save(patient);

        auditLogService.log(
            "CREATED",
            "Patient",
            String.valueOf(saved.getId()),
            "Patient registered: " + saved.getFirstName() + " " + saved.getLastName()
                + " (" + saved.getPatientNumber() + ")"
        );

        return toDto(saved);
    }

    public PatientDto updatePatient(Long id, PatientDto dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setBirthDate(dto.getBirthDate());
        patient.setGender(dto.getGender());
        patient.setAddress(dto.getAddress());
        patient.setContactNumber(dto.getContactNumber());
        patient.setEmergencyContact(dto.getEmergencyContact());

        Patient saved = patientRepository.save(patient);

        auditLogService.log(
            "UPDATED",
            "Patient",
            String.valueOf(saved.getId()),
            "Patient updated: " + saved.getFirstName() + " " + saved.getLastName()
        );

        return toDto(saved);
    }

    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        auditLogService.log(
            "DELETED",
            "Patient",
            String.valueOf(id),
            "Patient removed: " + patient.getFirstName() + " " + patient.getLastName()
                + " (" + patient.getPatientNumber() + ")"
        );

        patientRepository.deleteById(id);
    }

    private PatientDto toDto(Patient patient) {
        return PatientDto.builder()
                .id(patient.getId())
                .patientNumber(patient.getPatientNumber())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .contactNumber(patient.getContactNumber())
                .emergencyContact(patient.getEmergencyContact())
                .build();
    }
}