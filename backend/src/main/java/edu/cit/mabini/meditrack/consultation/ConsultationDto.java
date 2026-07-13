package edu.cit.mabini.meditrack.consultation;

import edu.cit.mabini.meditrack.patient.Patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConsultationDto {

    private Long id;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private String patientName;

    @NotBlank(message = "Chief complaint is required")
    private String chiefComplaint;

    private String vitalSigns;

    private String diagnosis;

    private String treatment;

    private String notes;

    private LocalDateTime consultationDate;

    private LocalDateTime createdAt;
}