package edu.cit.mabini.meditrack.doctor;

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
public class DoctorScheduleDto {

    private Long id;
    private String fullName;
    private String specialization;
    private String licenseNumber;
}
