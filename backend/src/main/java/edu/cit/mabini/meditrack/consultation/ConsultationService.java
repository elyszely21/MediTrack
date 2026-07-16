package edu.cit.mabini.meditrack.consultation;

import edu.cit.mabini.meditrack.appointment.AppointmentRepository;
import edu.cit.mabini.meditrack.common.audit.AuditLogService;
import edu.cit.mabini.meditrack.common.exception.AccessDeniedException;
import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import edu.cit.mabini.meditrack.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepo;
    private final PatientRepository patientRepo;
    private final AuditLogService auditLogService;
    private final AppointmentRepository appointmentRepository;

    public List<ConsultationDto> getByPatient(Long patientId) {
        authorizeDoctorPatientAccess(patientId);
        return consultationRepo.findByPatientId(patientId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ConsultationDto create(ConsultationDto dto) {
        Long requestedPatientId = dto.getPatientId();
        authorizeDoctorWriteForPatient(requestedPatientId);

        Patient patient = patientRepo.findById(requestedPatientId)
                .orElseThrow(() -> new RuntimeException(
                    "Patient not found with ID: " + requestedPatientId));

        Consultation consultation = Consultation.builder()
                .patient(patient)
                .chiefComplaint(dto.getChiefComplaint())
                .vitalSigns(dto.getVitalSigns())
                .diagnosis(dto.getDiagnosis())
                .treatment(dto.getTreatment())
                .notes(dto.getNotes())
                .consultationDate(
                    dto.getConsultationDate() != null
                        ? dto.getConsultationDate()
                        : LocalDateTime.now()
                )
                .build();

        Consultation saved = consultationRepo.save(consultation);

        auditLogService.log(
            "CREATED",
            "Consultation",
            String.valueOf(saved.getId()),
            "Consultation created for patient: "
                + patient.getFirstName() + " " + patient.getLastName()
                + " — CC: " + saved.getChiefComplaint()
        );

        return toDto(saved);
    }

    public ConsultationDto update(Long id, ConsultationDto dto) {
        Consultation consultation = consultationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(
                    "Consultation not found with ID: " + id));

        Long requestedPatientId = consultation.getPatient().getId();
        authorizeDoctorWriteForPatient(requestedPatientId);

        consultation.setChiefComplaint(dto.getChiefComplaint());
        consultation.setVitalSigns(dto.getVitalSigns());
        consultation.setDiagnosis(dto.getDiagnosis());
        consultation.setTreatment(dto.getTreatment());
        consultation.setNotes(dto.getNotes());

        if (dto.getConsultationDate() != null) {
            consultation.setConsultationDate(dto.getConsultationDate());
        }

        Consultation saved = consultationRepo.save(consultation);

        auditLogService.log(
            "UPDATED",
            "Consultation",
            String.valueOf(id),
            "Consultation updated — CC: " + saved.getChiefComplaint()
        );

        return toDto(saved);
    }

    public void delete(Long id) {
        Consultation consultation = consultationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found with ID: " + id));

        Long requestedPatientId = consultation.getPatient().getId();
        authorizeDoctorWriteForPatient(requestedPatientId);

        auditLogService.log(
            "DELETED",
            "Consultation",
            String.valueOf(id),
            "Consultation removed"
        );

        consultationRepo.deleteById(id);
    }

    private void authorizeDoctorPatientAccess(Long requestedPatientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String role = resolveRole(auth);
        if ("SUPER_ADMIN".equals(role)) {
            throw new AccessDeniedException("SUPER_ADMIN is forbidden to access consultations");
        }

        if ("DOCTOR".equals(role)) {
            Long doctorId = resolveAuthenticatedDoctorId(auth);
            if (!appointmentRepository.existsByPatientIdAndDoctorId(requestedPatientId, doctorId)) {
                throw new AccessDeniedException("Doctor is not assigned to this patient");
            }
            return;
        }

        if ("PATIENT".equals(role)) {
            Long selfPatientId = resolvePatientIdForAuthenticatedPatient(auth.getName());
            if (!selfPatientId.equals(requestedPatientId)) {
                throw new AccessDeniedException("Access denied to other patients' consultations");
            }
            return;
        }

        if ("NURSE".equals(role)) {
            // controller method-level grants nurse read, but service-level should allow read-only
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    private void authorizeDoctorWriteForPatient(Long requestedPatientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String role = resolveRole(auth);
        if (!"DOCTOR".equals(role)) {
            throw new AccessDeniedException("Only DOCTOR can create/update/delete consultations");
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
        Patient patient = patientRepo.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("No patient profile found for this account"));
        return patient.getId();
    }

    private ConsultationDto toDto(Consultation c) {
        ConsultationDto dto = new ConsultationDto();
        dto.setId(c.getId());
        dto.setPatientId(c.getPatient().getId());
        dto.setPatientName(
            c.getPatient().getFirstName() + " " + c.getPatient().getLastName()
        );
        dto.setChiefComplaint(c.getChiefComplaint());
        dto.setVitalSigns(c.getVitalSigns());
        dto.setDiagnosis(c.getDiagnosis());
        dto.setTreatment(c.getTreatment());
        dto.setNotes(c.getNotes());
        dto.setConsultationDate(c.getConsultationDate());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}

