package edu.cit.mabini.meditrack.reports;

import edu.cit.mabini.meditrack.billing.Bill;
import edu.cit.mabini.meditrack.appointment.Appointment;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryDto {

    private long totalPatients;
    private long totalNurses;
    private long totalAppointments;
    private long totalConsultations;
    private long totalPrescriptions;
    private long totalBills;
    private long totalMedicalRecords;

    private long todayAppointments;

    private long scheduledAppointments;
    private long approvedAppointments;
    private long completedAppointments;
    private long cancelledAppointments;

    private long unpaidBills;
    private long partialBills;
    private long paidBills;

    private List<RecentActivityDto> recentActivities;
}