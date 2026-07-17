package edu.cit.mabini.meditrack.appointment;

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
public class DoctorDashboardDto {

    private long todayAppointments;
    private long pendingAppointments;
    private long upcomingAppointments;
    private long totalAppointments;
    private long totalAssignedPatients;
}
