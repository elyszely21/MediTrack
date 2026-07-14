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

        long pending    = appointmentRepo.countByStatus(Appointment.AppointmentStatus.PENDING_APPROVAL);
        long approved   = appointmentRepo.countByStatus(Appointment.AppointmentStatus.APPROVED);
        long completed  = appointmentRepo.countByStatus(Appointment.AppointmentStatus.COMPLETED);
        long cancelled  = appointmentRepo.countByStatus(Appointment.AppointmentStatus.CANCELLED);


        long unpaid     = billRepo.countByStatus("UNPAID");
        long partial    = billRepo.countByStatus("PARTIAL");
        long paid       = billRepo.countByStatus("PAID");

        List<RecentActivityDto> activities = auditLogRepo
                .findTop20ByOrderByPerformedAtDesc()
                .stream()
                .map(this::toActivityDto)
                .collect(Collectors.toList());

        return DashboardSummaryDto.builder()
                .totalPatients(patientRepo.count())
                .totalNurses(userRepo.countByRole("NURSE"))
                .totalAppointments(appointmentRepo.count())
                .totalConsultations(consultationRepo.count())
                .totalPrescriptions(prescriptionRepo.count())
                .totalBills(billRepo.count())
                .totalMedicalRecords(medicalRecordRepo.count())
                .todayAppointments(todayAppointments)
                .scheduledAppointments(pending)
                .approvedAppointments(approved)
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