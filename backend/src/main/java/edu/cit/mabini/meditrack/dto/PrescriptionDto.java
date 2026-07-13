package edu.cit.mabini.meditrack.prescription;

import edu.cit.mabini.meditrack.patient.Patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDto {
    private Long id;

    @NotNull(message = "Patient is required")
    private Long patientId;

    private Long medicalRecordId;

    @NotBlank(message = "Medication is required")
    private String medication;

    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;

    @NotBlank(message = "Prescriber name is required")
    private String prescribedBy;

    @NotNull(message = "Prescribed date is required")
    private LocalDate prescribedDate;

    private String status;
}