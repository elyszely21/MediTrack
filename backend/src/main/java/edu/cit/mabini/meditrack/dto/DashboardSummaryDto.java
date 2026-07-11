package edu.cit.mabini.meditrack.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryDto {

    // Counts
    private long totalPatients;
    private long totalNurses;
    private long totalAppointments;
    private long totalConsultations;
    private long totalPrescriptions;
    private long totalBills;
    private long totalMedicalRecords;

    // Today
    private long todayAppointments;

    // Appointment breakdown by status
    private long scheduledAppointments;
    private long completedAppointments;
    private long cancelledAppointments;

    // Bill breakdown
    private long unpaidBills;
    private long partialBills;
    private long paidBills;

    // Recent activity
    private List<RecentActivityDto> recentActivities;
}