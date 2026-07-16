package edu.cit.mabini.meditrack.prescription;

import edu.cit.mabini.meditrack.appointment.AppointmentRepository;
import edu.cit.mabini.meditrack.medicalrecord.MedicalRecord;
import edu.cit.mabini.meditrack.medicalrecord.MedicalRecordRepository;
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
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    public List<PrescriptionDto> getByPatient(Long patientId) {
        authorizePatientReadForPrescription(patientId);
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    public PrescriptionDto create(PrescriptionDto dto) {
        Long requestedPatientId = dto.getPatientId();
        authorizePatientWriteForPrescription(requestedPatientId, PrescriptionAction.CREATE);

        Patient patient = patientRepository.findById(requestedPatientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        MedicalRecord record = null;
        if (dto.getMedicalRecordId() != null) {
            record = medicalRecordRepository.findById(dto.getMedicalRecordId())
                    .orElseThrow(() -> new IllegalArgumentException("Medical record not found"));
        }

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .medicalRecord(record)
                .medication(dto.getMedication())
                .dosage(dto.getDosage())
                .frequency(dto.getFrequency())
                .duration(dto.getDuration())
                .instructions(dto.getInstructions())
                .prescribedBy(dto.getPrescribedBy())
                .prescribedDate(dto.getPrescribedDate())
                .status(dto.getStatus() == null || dto.getStatus().isBlank() ? "ACTIVE" : dto.getStatus())
                .build();

        return toDto(prescriptionRepository.save(prescription));
    }

    public PrescriptionDto update(Long id, PrescriptionDto dto) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));

        Long requestedPatientId = prescription.getPatient().getId();
        authorizePatientWriteForPrescription(requestedPatientId, PrescriptionAction.UPDATE);

        prescription.setMedication(dto.getMedication());
        prescription.setDosage(dto.getDosage());
        prescription.setFrequency(dto.getFrequency());
        prescription.setDuration(dto.getDuration());
        prescription.setInstructions(dto.getInstructions());
        prescription.setPrescribedBy(dto.getPrescribedBy());
        prescription.setPrescribedDate(dto.getPrescribedDate());
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            prescription.setStatus(dto.getStatus());
        }

        return toDto(prescriptionRepository.save(prescription));
    }

    public void delete(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));

        Long requestedPatientId = prescription.getPatient().getId();
        authorizePatientWriteForPrescription(requestedPatientId, PrescriptionAction.DELETE);

        prescriptionRepository.deleteById(id);
    }

    private enum PrescriptionAction {
        CREATE, UPDATE, DELETE
    }

    private void authorizePatientReadForPrescription(Long requestedPatientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String role = resolveRole(auth);
        if ("SUPER_ADMIN".equals(role)) {
            throw new AccessDeniedException("SUPER_ADMIN is forbidden to access prescriptions");
        }

        if ("PATIENT".equals(role)) {
            Long selfPatientId = resolvePatientIdForAuthenticatedPatient(auth.getName());
            if (!selfPatientId.equals(requestedPatientId)) {
                throw new AccessDeniedException("Access denied to other patients' prescriptions");
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

        if ("NURSE".equals(role)) {
            // Read-only access already granted at controller level
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    private void authorizePatientWriteForPrescription(Long requestedPatientId, PrescriptionAction action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String role = resolveRole(auth);

        if ("SUPER_ADMIN".equals(role)) {
            throw new AccessDeniedException("SUPER_ADMIN is forbidden to create/update/delete prescriptions");
        }

        if ("NURSE".equals(role)) {
            // Never create/update/delete
            throw new AccessDeniedException("NURSE cannot create/update/delete prescriptions");
        }

        if ("PATIENT".equals(role)) {
            // Patient read only
            throw new AccessDeniedException("PATIENT cannot create/update/delete prescriptions");
        }

        if ("DOCTOR".equals(role)) {
            Long doctorId = resolveAuthenticatedDoctorId(auth);
            if (!appointmentRepository.existsByPatientIdAndDoctorId(requestedPatientId, doctorId)) {
                throw new AccessDeniedException("Doctor is not assigned to this patient");
            }
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    private String resolveRole(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getRole();
        }
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

    private PrescriptionDto toDto(Prescription p) {
        return PrescriptionDto.builder()
                .id(p.getId())
                .patientId(p.getPatient().getId())
                .medicalRecordId(p.getMedicalRecord() != null ? p.getMedicalRecord().getId() : null)
                .medication(p.getMedication())
                .dosage(p.getDosage())
                .frequency(p.getFrequency())
                .duration(p.getDuration())
                .instructions(p.getInstructions())
                .prescribedBy(p.getPrescribedBy())
                .prescribedDate(p.getPrescribedDate())
                .status(p.getStatus())
                .build();
    }
}

