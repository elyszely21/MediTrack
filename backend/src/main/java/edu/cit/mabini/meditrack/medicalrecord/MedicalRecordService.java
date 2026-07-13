package edu.cit.mabini.meditrack.medicalrecord;

import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;

    public List<MedicalRecordDto> getRecordsByPatient(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId).stream().map(this::toDto).collect(Collectors.toList());
    }

    public MedicalRecordDto createRecord(MedicalRecordDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
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
