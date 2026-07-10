package edu.cit.mabini.meditrack.controller;

import edu.cit.mabini.meditrack.dto.BillDto;
import edu.cit.mabini.meditrack.dto.PaymentDto;
import edu.cit.mabini.meditrack.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<BillDto>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getBillsByPatient(patientId));
    }

    @PostMapping
    public ResponseEntity<BillDto> create(@Valid @RequestBody BillDto dto) {
        return ResponseEntity.status(201).body(billingService.createBill(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billingService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{billId}/payments")
    public ResponseEntity<List<PaymentDto>> getPayments(@PathVariable Long billId) {
        return ResponseEntity.ok(billingService.getPayments(billId));
    }

    @PostMapping("/{billId}/payments")
    public ResponseEntity<?> recordPayment(@PathVariable Long billId, @Valid @RequestBody PaymentDto dto) {
        try {
            return ResponseEntity.status(201).body(billingService.recordPayment(billId, dto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }
}