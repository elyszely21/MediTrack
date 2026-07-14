package edu.cit.mabini.meditrack.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDto {
    private Long id;

    @NotNull(message = "patient is required")
    private Long patientId;

    private String patientName;
    private String patientNumber;

    @NotNull(message = "doctor is required")
    private Long doctorId;

    private String appointmentNumber;

    @NotBlank(message = "appointmentType is required")
    private String appointmentType;

    private Integer queueNumber;

    @Positive(message = "durationMinutes must be greater than zero")
    private Integer durationMinutes;

    private Integer priority;
    private String notes;

    @NotNull(message = "appointmentDate is required")
    private LocalDate appointmentDate;

    @NotNull(message = "startTime is required")
    private LocalTime appointmentTime;

    @NotNull(message = "endTime is required")
    private LocalTime endTime;

    private Appointment.AppointmentStatus status;

    @NotBlank(message = "reason is required")
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

