package edu.cit.mabini.meditrack.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientLookupDto {
    private Long id;
    private String patientNumber;
    private String fullName;
}

