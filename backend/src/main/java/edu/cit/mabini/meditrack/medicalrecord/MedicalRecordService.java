package edu.cit.mabini.meditrack.medicalrecord;

import edu.cit.mabini.meditrack.appointment.Appointment;
import edu.cit.mabini.meditrack.appointment.AppointmentRepository;
import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import edu.cit.mabini.meditrack.common.exception.AccessDeniedException;
import edu.cit.mabini.meditrack.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public List<MedicalRecordDto> getRecordsByPatient(Long patientId) {
        authorizePatientReadForMedicalRecord(patientId);
        return medicalRecordRepository.findByPatientId(patientId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MedicalRecordDto createRecord(MedicalRecordDto dto) {
        Long requestedPatientId = dto.getPatientId();
        authorizePatientWriteForMedicalRecord(requestedPatientId);

        Patient patient = patientRepository.findById(requestedPatientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .diagnosis(dto.getDiagnosis())
                .treatment(dto.getTreatment())
                .prescription(dto.getPrescription())
                .notes(dto.getNotes())
                .visitDate(dto.getVisitDate())
                .build();

        return toDto(medicalRecordRepository.save(record));
    }

    private void authorizePatientReadForMedicalRecord(Long requestedPatientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String role = resolveRole(auth);
        if ("SUPER_ADMIN".equals(role)) {
            throw new AccessDeniedException("SUPER_ADMIN is forbidden to access medical records");
        }

        if ("PATIENT".equals(role)) {
            Long selfPatientId = resolvePatientIdForAuthenticatedPatient(auth.getName());
            if (!selfPatientId.equals(requestedPatientId)) {
                throw new AccessDeniedException("Access denied to other patients' medical records");
            }
            return;
        }

        if ("DOCTOR".equals(role)) {
            Long doctorId = resolveAuthenticatedDoctorId(auth);
            if (!appointmentRepository.existsByPatientIdAndDoctorId(requestedPatientId, doctorId)) {
                throw new AccessDeniedException("Doctor is not assigned to this patient");
            }
            return;
        }

        // NURSE: not allowed to modify/own medical records per your RBAC; read access not granted here.
        throw new AccessDeniedException("Access denied");
    }

    private void authorizePatientWriteForMedicalRecord(Long requestedPatientId) {
        // Writing medical records: only DOCTOR allowed (per Phase rules implied; controller-level @PreAuthorize limits create to DOCTOR)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String role = resolveRole(auth);
        if (!"DOCTOR".equals(role)) {
            throw new AccessDeniedException("Only DOCTOR can create medical records");
        }

        Long doctorId = resolveAuthenticatedDoctorId(auth);
        if (!appointmentRepository.existsByPatientIdAndDoctorId(requestedPatientId, doctorId)) {
            throw new AccessDeniedException("Doctor is not assigned to this patient");
        }
    }

    private String resolveRole(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getRole();
        }
        // Fallback from authorities
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .map(s -> s != null && s.startsWith("ROLE_") ? s.substring("ROLE_".length()) : s)
                .orElse("");
    }

    private Long resolveAuthenticatedDoctorId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }
        throw new AccessDeniedException("Invalid authentication principal");
    }

    private Long resolvePatientIdForAuthenticatedPatient(String email) {
        Patient patient = patientRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("No patient profile found for this account"));
        return patient.getId();
    }

    private MedicalRecordDto toDto(MedicalRecord record) {
        return MedicalRecordDto.builder()
                .id(record.getId())
                .patientId(record.getPatient().getId())
                .diagnosis(record.getDiagnosis())
                .treatment(record.getTreatment())
                .prescription(record.getPrescription())
                .notes(record.getNotes())
                .visitDate(record.getVisitDate())
                .build();
    }
}

