package edu.cit.mabini.meditrack.consultation;

import edu.cit.mabini.meditrack.common.audit.AuditLogService;

import edu.cit.mabini.meditrack.consultation.ConsultationDto;
import edu.cit.mabini.meditrack.consultation.Consultation;
import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.consultation.ConsultationRepository;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepo;
    private final PatientRepository      patientRepo;
    private final AuditLogService        auditLogService;

    public List<ConsultationDto> getByPatient(Long patientId) {
        return consultationRepo.findByPatientId(patientId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ConsultationDto create(ConsultationDto dto) {
        Patient patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException(
                    "Patient not found with ID: " + dto.getPatientId()));

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
        if (!consultationRepo.existsById(id)) {
            throw new RuntimeException("Consultation not found with ID: " + id);
        }

        auditLogService.log(
            "DELETED",
            "Consultation",
            String.valueOf(id),
            "Consultation removed"
        );

        consultationRepo.deleteById(id);
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