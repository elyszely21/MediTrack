package edu.cit.mabini.meditrack.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NurseDto {
    private Long id;
    private String fullName;
    private String email;
    private String contactNumber;

    private String licenseNumber;
    private String specialization;

    private String role;
    private LocalDateTime createdAt;
}

