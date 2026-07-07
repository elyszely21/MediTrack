package edu.cit.mabini.meditrack.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDto {
    private Long id;
    private Long patientId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String remarks;
}
