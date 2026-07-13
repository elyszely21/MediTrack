package edu.cit.mabini.meditrack.appointment;

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
    private Long patientId;
    private String patientName;
    private String patientNumber;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
}