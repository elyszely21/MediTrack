package edu.cit.mabini.meditrack.prescription;

import edu.cit.mabini.meditrack.medicalrecord.MedicalRecord;
import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.medicalrecord.MedicalRecordRepository;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public List<PrescriptionDto> getByPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    public PrescriptionDto create(PrescriptionDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
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
        if (!prescriptionRepository.existsById(id)) {
            throw new IllegalArgumentException("Prescription not found");
        }
        prescriptionRepository.deleteById(id);
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