package edu.cit.mabini.meditrack.medicalrecord;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordDto {
    private Long id;
    private Long patientId;
    private String diagnosis;
    private String treatment;
    private String prescription;
    private String notes;
    private LocalDate visitDate;
}
