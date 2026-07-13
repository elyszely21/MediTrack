package edu.cit.mabini.meditrack.reports;

import edu.cit.mabini.meditrack.billing.BillRepository;

import edu.cit.mabini.meditrack.billing.Bill;

import edu.cit.mabini.meditrack.medicalrecord.MedicalRecordRepository;

import edu.cit.mabini.meditrack.prescription.PrescriptionRepository;

import edu.cit.mabini.meditrack.consultation.ConsultationRepository;

import edu.cit.mabini.meditrack.appointment.AppointmentRepository;

import edu.cit.mabini.meditrack.appointment.Appointment;

import edu.cit.mabini.meditrack.patient.PatientRepository;

import edu.cit.mabini.meditrack.user.UserRepository;

import edu.cit.mabini.meditrack.common.audit.AuditLogRepository;

import edu.cit.mabini.meditrack.common.audit.AuditLog;
import edu.cit.mabini.meditrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final PatientRepository       patientRepo;
    private final UserRepository          userRepo;
    private final AppointmentRepository   appointmentRepo;
    private final ConsultationRepository  consultationRepo;
    private final PrescriptionRepository  prescriptionRepo;
    private final BillRepository          billRepo;
    private final MedicalRecordRepository medicalRecordRepo;
    private final AuditLogRepository      auditLogRepo;

    public DashboardSummaryDto getSummary() {

        // Today's appointments
        long todayAppointments = appointmentRepo
                .findByAppointmentDate(LocalDate.now()).size();

        // Appointment status breakdown
        long scheduled  = appointmentRepo.countByStatus("SCHEDULED");
        long completed  = appointmentRepo.countByStatus("COMPLETED");
        long cancelled  = appointmentRepo.countByStatus("CANCELLED");

        // Bill status breakdown
        long unpaid     = billRepo.countByStatus("UNPAID");
        long partial    = billRepo.countByStatus("PARTIAL");
        long paid       = billRepo.countByStatus("PAID");

        // Recent activities
        List<RecentActivityDto> activities = auditLogRepo
                .findTop20ByOrderByPerformedAtDesc()
                .stream()
                .map(this::toActivityDto)
                .collect(Collectors.toList());

        return DashboardSummaryDto.builder()
                .totalPatients(patientRepo.count())
                .totalNurses(userRepo.countByRole("ROLE_NURSE"))
                .totalAppointments(appointmentRepo.count())
                .totalConsultations(consultationRepo.count())
                .totalPrescriptions(prescriptionRepo.count())
                .totalBills(billRepo.count())
                .totalMedicalRecords(medicalRecordRepo.count())
                .todayAppointments(todayAppointments)
                .scheduledAppointments(scheduled)
                .completedAppointments(completed)
                .cancelledAppointments(cancelled)
                .unpaidBills(unpaid)
                .partialBills(partial)
                .paidBills(paid)
                .recentActivities(activities)
                .build();
    }

    private RecentActivityDto toActivityDto(AuditLog log) {
        return RecentActivityDto.builder()
                .id(log.getId())
                .action(log.getAction())
                .entity(log.getEntity())
                .entityId(log.getEntityId())
                .performedBy(log.getPerformedBy())
                .details(log.getDetails())
                .performedAt(log.getPerformedAt())
                .build();
    }
}