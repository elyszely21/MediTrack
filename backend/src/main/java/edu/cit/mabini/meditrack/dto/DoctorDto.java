package edu.cit.mabini.meditrack.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DoctorDto {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String specialization;
    private String licenseNumber;
    private String role;
    private LocalDateTime createdAt;
}