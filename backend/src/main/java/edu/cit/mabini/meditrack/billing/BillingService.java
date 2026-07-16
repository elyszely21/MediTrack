package edu.cit.mabini.meditrack.billing;

import edu.cit.mabini.meditrack.appointment.Appointment;
import edu.cit.mabini.meditrack.appointment.AppointmentRepository;
import edu.cit.mabini.meditrack.common.audit.AuditLogService;
import edu.cit.mabini.meditrack.common.exception.AccessDeniedException;
import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import edu.cit.mabini.meditrack.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogService auditLogService;

    public List<BillDto> getBillsByPatient(Long patientId) {
        authorizeBillingRead(patientId);
        return billRepository.findByPatientId(patientId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BillDto createBill(BillDto dto) {
        authorizeBillingWrite(dto.getPatientId());

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        Appointment appointment = null;
        if (dto.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        }

        Bill bill = Bill.builder()
                .patient(patient)
                .appointment(appointment)
                .description(dto.getDescription())
                .amount(dto.getAmount())
                .balance(dto.getAmount())
                .status("UNPAID")
                .billDate(dto.getBillDate())
                .build();

        Bill saved = billRepository.save(bill);

        auditLogService.log(
            "CREATED",
            "Bill",
            String.valueOf(saved.getId()),
            "Bill created for patient: "
                + patient.getFirstName() + " " + patient.getLastName()
                + " — " + saved.getDescription()
                + " ₱" + saved.getAmount()
        );

        return toDto(saved);
    }

    public void deleteBill(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));

        authorizeBillingDelete(bill.getPatient().getId());

        auditLogService.log(
            "DELETED",
            "Bill",
            String.valueOf(id),
            "Bill removed"
        );

        billRepository.deleteById(id);
    }

    public List<PaymentDto> getPayments(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));

        authorizeBillingRead(bill.getPatient().getId());

        return paymentRepository.findByBillIdOrderByPaymentDateAsc(billId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDto recordPayment(Long billId, PaymentDto dto) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));

        authorizeBillingWrite(bill.getPatient().getId());

        if (dto.getAmountPaid().compareTo(bill.getBalance()) > 0) {
            throw new IllegalArgumentException(
                "Payment exceeds remaining balance of " + bill.getBalance()
            );
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .amountPaid(dto.getAmountPaid())
                .paymentDate(dto.getPaymentDate())
                .paymentMethod(dto.getPaymentMethod())
                .receivedBy(dto.getReceivedBy())
                .build();

        Payment saved = paymentRepository.save(payment);

        BigDecimal newBalance = bill.getBalance().subtract(dto.getAmountPaid());
        bill.setBalance(newBalance);
        bill.setStatus(
            newBalance.compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PARTIALLY_PAID"
        );
        billRepository.save(bill);

        auditLogService.log(
            "PAYMENT",
            "Bill",
            String.valueOf(billId),
            "Payment of ₱" + dto.getAmountPaid()
                + " recorded via " + dto.getPaymentMethod()
                + " — remaining balance: ₱" + newBalance
        );

        return toDto(saved);
    }

    private void authorizeBillingRead(Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String role = resolveRole(auth);
        if ("SUPER_ADMIN".equals(role)) {
            return;
        }

        if ("PATIENT".equals(role)) {
            Long selfPatientId = resolvePatientIdForAuthenticatedPatient(auth.getName());
            if (!selfPatientId.equals(patientId)) {
                throw new AccessDeniedException("Access denied to other patients' billing");
            }
            return;
        }

        // Doctor + Nurse forbidden
        throw new AccessDeniedException("Access denied");
    }

    private void authorizeBillingWrite(Long patientId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        String role = resolveRole(auth);
        if ("SUPER_ADMIN".equals(role)) {
            return;
        }

        throw new AccessDeniedException("Only SUPER_ADMIN can manage billing");
    }

    private void authorizeBillingDelete(Long patientId) {
        authorizeBillingWrite(patientId);
    }

    private String resolveRole(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getRole();
        }
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .map(s -> s != null && s.startsWith("ROLE_") ? s.substring("ROLE_".length()) : s)
                .orElse("");
    }

    private Long resolvePatientIdForAuthenticatedPatient(String email) {
        Patient patient = patientRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("No patient profile found for this account"));
        return patient.getId();
    }

    private BillDto toDto(Bill bill) {
        return BillDto.builder()
                .id(bill.getId())
                .patientId(bill.getPatient().getId())
                .appointmentId(
                    bill.getAppointment() != null
                        ? bill.getAppointment().getId() : null
                )
                .description(bill.getDescription())
                .amount(bill.getAmount())
                .balance(bill.getBalance())
                .status(bill.getStatus())
                .billDate(bill.getBillDate())
                .build();
    }

    private PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .billId(payment.getBill().getId())
                .amountPaid(payment.getAmountPaid())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .receivedBy(payment.getReceivedBy())
                .build();
    }
}

