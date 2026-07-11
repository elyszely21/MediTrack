package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.ConsultationDto;
import edu.cit.mabini.meditrack.entity.Consultation;
import edu.cit.mabini.meditrack.entity.Patient;
import edu.cit.mabini.meditrack.repository.ConsultationRepository;
import edu.cit.mabini.meditrack.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepo;
    private final PatientRepository patientRepo;

    // ── Get all consultations for a patient ───────────────────────────────────

    public List<ConsultationDto> getByPatient(Long patientId) {
        return consultationRepo.findByPatientId(patientId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public ConsultationDto create(ConsultationDto dto) {
        Patient patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + dto.getPatientId()));

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

        return toDto(consultationRepo.save(consultation));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public ConsultationDto update(Long id, ConsultationDto dto) {
        Consultation consultation = consultationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found with ID: " + id));

        consultation.setChiefComplaint(dto.getChiefComplaint());
        consultation.setVitalSigns(dto.getVitalSigns());
        consultation.setDiagnosis(dto.getDiagnosis());
        consultation.setTreatment(dto.getTreatment());
        consultation.setNotes(dto.getNotes());

        if (dto.getConsultationDate() != null) {
            consultation.setConsultationDate(dto.getConsultationDate());
        }

        return toDto(consultationRepo.save(consultation));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void delete(Long id) {
        if (!consultationRepo.existsById(id)) {
            throw new RuntimeException("Consultation not found with ID: " + id);
        }
        consultationRepo.deleteById(id);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ConsultationDto toDto(Consultation c) {
        ConsultationDto dto = new ConsultationDto();
        dto.setId(c.getId());
        dto.setPatientId(c.getPatient().getId());
        dto.setPatientName(c.getPatient().getFirstName() + " " + c.getPatient().getLastName());
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