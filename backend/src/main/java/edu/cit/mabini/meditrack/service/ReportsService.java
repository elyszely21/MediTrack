package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.DashboardSummaryDto;
import edu.cit.mabini.meditrack.dto.RecentActivityDto;
import edu.cit.mabini.meditrack.entity.AuditLog;
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

        long todayAppointments = appointmentRepo
                .findByAppointmentDate(LocalDate.now()).size();

        long scheduled = appointmentRepo.countByStatus("SCHEDULED");
        long completed = appointmentRepo.countByStatus("COMPLETED");
        long cancelled = appointmentRepo.countByStatus("CANCELLED");

        long unpaid  = billRepo.countByStatus("UNPAID");
        long partial = billRepo.countByStatus("PARTIALLY_PAID");
        long paid    = billRepo.countByStatus("PAID");

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