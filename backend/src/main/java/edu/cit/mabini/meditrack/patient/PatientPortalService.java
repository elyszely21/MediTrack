package edu.cit.mabini.meditrack.patient;

import edu.cit.mabini.meditrack.appointment.AppointmentDto;
import edu.cit.mabini.meditrack.appointment.AppointmentService;
import edu.cit.mabini.meditrack.billing.BillingService;
import edu.cit.mabini.meditrack.billing.BillDto;
import edu.cit.mabini.meditrack.billing.PaymentDto;
import edu.cit.mabini.meditrack.common.audit.AuditLog;
import edu.cit.mabini.meditrack.common.audit.AuditLogService;
import edu.cit.mabini.meditrack.medicalrecord.MedicalRecordDto;
import edu.cit.mabini.meditrack.medicalrecord.MedicalRecordService;
import edu.cit.mabini.meditrack.prescription.PrescriptionDto;
import edu.cit.mabini.meditrack.prescription.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientPortalService {

    private final PatientRepository patientRepository;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final MedicalRecordService medicalRecordService;
    private final BillingService billingService;
    private final AuditLogService auditLogService;

    // ── Resolve the caller's own patient record ─────────────────────────────

    private Patient resolvePatient(String email) {
        return patientRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No patient profile found for this account."
                ));
    }

    private Patient resolveAuthenticatedPatient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Not authenticated");
        }
        return resolvePatient(auth.getName());
    }

    // ── Profile ──────────────────────────────────────────────────────────────

    public PatientProfileDto getMyProfile(String email) {
        Patient patient = resolvePatient(email);

        return PatientProfileDto.builder()
                .id(patient.getId())
                .patientNumber(patient.getPatientNumber())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .contactNumber(patient.getContactNumber())
                .emergencyContact(patient.getEmergencyContact())
                .email(patient.getEmail())
                .phoneNumber(patient.getContactNumber())
                .build();
    }

    // ── Update the editable parts of my own profile ─────────────────────────

    public PatientProfileDto updateMyProfile(String email, PatientProfileDto dto) {
        Patient patient = resolvePatient(email);

        patient.setBirthDate(dto.getBirthDate());
        patient.setGender(dto.getGender());
        patient.setAddress(dto.getAddress());
        patient.setContactNumber(dto.getContactNumber());
        patient.setEmergencyContact(dto.getEmergencyContact());

        patientRepository.save(patient);
        return getMyProfile(email);
    }

    // ── My clinical data (read-only) ────────────────────────────────────────

    public List<AppointmentDto> getMyAppointments(String email) {
        return appointmentService.getAppointmentsByPatient(resolvePatient(email).getId());
    }

    public List<PrescriptionDto> getMyPrescriptions(String email) {
        return prescriptionService.getByPatient(resolvePatient(email).getId());
    }

    public List<MedicalRecordDto> getMyMedicalRecords(String email) {
        return medicalRecordService.getRecordsByPatient(resolvePatient(email).getId());
    }

    // ── Billing (read-only) ─────────────────────────────────────────────────

    public PatientPortalBillingDto getMyBilling(String email) {
        // Never trust any patient identifier from the client.
        Patient patient = resolveAuthenticatedPatient();

        List<BillDto> bills = billingService.getBillsByPatient(patient.getId());
        // payments are fetched per-bill; reusing BillingService means we should not duplicate payment logic.
        // For performance/consistency, BillingService already provides payments by bill id.
        // The PatientPortalBillingDto contract expects payments grouped alongside bills.
        List<PaymentDto> payments = bills.stream()
                .flatMap(bill -> billingService.getPayments(bill.getId()).stream())
                .toList();

        return PatientPortalBillingDto.builder()
                .bills(bills)
                .payments(payments)
                .build();
    }

    // ── Access History ───────────────────────────────────────────────────────

    public List<PatientPortalAccessHistoryDto> getMyAccessHistory(String email) {
        // Never trust any patient identifier from the client.
        Patient patient = resolveAuthenticatedPatient();

        // NOTE: We will only filter using fields that are already present in AuditLog.
        // AuditLog must be able to link events to patientId reliably via entityId.
        // If entityId does not represent patientId consistently, filtering would be unreliable.
        List<AuditLog> recent = auditLogService.getRecentAuditLogs();

        return recent.stream()
                .filter(log -> String.valueOf(patient.getId()).equals(String.valueOf(log.getEntityId())))
                .map(log -> PatientPortalAccessHistoryDto.builder()
                        .user(log.getPerformedBy())
                        .role(null)
                        .action(log.getAction())
                        .recordType(log.getEntity())
                        .timestamp(log.getPerformedAt())
                        .build())
                .sorted(Comparator.comparing(PatientPortalAccessHistoryDto::getTimestamp).reversed())
                .toList();
    }
}
