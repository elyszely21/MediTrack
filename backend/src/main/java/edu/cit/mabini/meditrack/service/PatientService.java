package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.PatientDto;
import edu.cit.mabini.meditrack.entity.Patient;
import edu.cit.mabini.meditrack.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final AuditLogService   auditLogService;

    // ── Get all active patients ───────────────────────────────────────────────

    public List<PatientDto> findAllPatients() {
        return patientRepository.findByArchivedFalse()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Get all archived patients ─────────────────────────────────────────────

    public List<PatientDto> findArchivedPatients() {
        return patientRepository.findByArchivedTrue()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Search patients ───────────────────────────────────────────────────────

    public List<PatientDto> searchPatients(String q, boolean archived) {
        List<Patient> results = archived
                ? patientRepository.searchArchivedPatients(q)
                : patientRepository.searchActivePatients(q);
        return results.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ── Get single patient ────────────────────────────────────────────────────

    public PatientDto getPatient(Long id) {
        return patientRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
    }

    // ── Create ────────────────────────────────────────────────────────────────

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
                .archived(false)
                .build();

        Patient saved = patientRepository.save(patient);

        auditLogService.log(
            "CREATED",
            "Patient",
            String.valueOf(saved.getId()),
            "Patient registered: " + saved.getFirstName()
                + " " + saved.getLastName()
                + " (" + saved.getPatientNumber() + ")"
        );

        return toDto(saved);
    }

    // ── Update ────────────────────────────────────────────────────────────────

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
            "Patient updated: " + saved.getFirstName()
                + " " + saved.getLastName()
        );

        return toDto(saved);
    }

    // ── Archive ───────────────────────────────────────────────────────────────

    public PatientDto archivePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        patient.setArchived(true);
        Patient saved = patientRepository.save(patient);

        auditLogService.log(
            "ARCHIVED",
            "Patient",
            String.valueOf(id),
            "Patient archived: " + patient.getFirstName()
                + " " + patient.getLastName()
        );

        return toDto(saved);
    }

    // ── Unarchive ─────────────────────────────────────────────────────────────

    public PatientDto unarchivePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        patient.setArchived(false);
        Patient saved = patientRepository.save(patient);

        auditLogService.log(
            "UNARCHIVED",
            "Patient",
            String.valueOf(id),
            "Patient restored: " + patient.getFirstName()
                + " " + patient.getLastName()
        );

        return toDto(saved);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deletePatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        auditLogService.log(
            "DELETED",
            "Patient",
            String.valueOf(id),
            "Patient permanently deleted: " + patient.getFirstName()
                + " " + patient.getLastName()
                + " (" + patient.getPatientNumber() + ")"
        );

        patientRepository.deleteById(id);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

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
                .archived(patient.getArchived())
                .build();
    }
}