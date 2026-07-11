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