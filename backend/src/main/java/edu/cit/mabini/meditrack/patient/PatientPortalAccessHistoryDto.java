package edu.cit.mabini.meditrack.patient;

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
public class PatientPortalAccessHistoryDto {
    private String user;
    private String role;
    private String action;
    private String recordType;
    private LocalDateTime timestamp;
}

