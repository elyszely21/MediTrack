package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.BillDto;
import edu.cit.mabini.meditrack.dto.PaymentDto;
import edu.cit.mabini.meditrack.entity.Appointment;
import edu.cit.mabini.meditrack.entity.Bill;
import edu.cit.mabini.meditrack.entity.Patient;
import edu.cit.mabini.meditrack.entity.Payment;
import edu.cit.mabini.meditrack.repository.AppointmentRepository;
import edu.cit.mabini.meditrack.repository.BillRepository;
import edu.cit.mabini.meditrack.repository.PatientRepository;
import edu.cit.mabini.meditrack.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillRepository        billRepository;
    private final PaymentRepository     paymentRepository;
    private final PatientRepository     patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditLogService       auditLogService;

    public List<BillDto> getBillsByPatient(Long patientId) {
        return billRepository.findByPatientId(patientId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BillDto createBill(BillDto dto) {
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
        if (!billRepository.existsById(id)) {
            throw new IllegalArgumentException("Bill not found");
        }

        auditLogService.log(
            "DELETED",
            "Bill",
            String.valueOf(id),
            "Bill removed"
        );

        billRepository.deleteById(id);
    }

    public List<PaymentDto> getPayments(Long billId) {
        return paymentRepository.findByBillIdOrderByPaymentDateAsc(billId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDto recordPayment(Long billId, PaymentDto dto) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found"));

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