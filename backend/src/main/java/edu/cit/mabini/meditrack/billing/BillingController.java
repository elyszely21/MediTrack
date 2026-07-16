package edu.cit.mabini.meditrack.billing;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<BillDto>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(billingService.getBillsByPatient(patientId));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BillDto> create(@Valid @RequestBody BillDto dto) {
        return ResponseEntity.status(201).body(billingService.createBill(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        billingService.deleteBill(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{billId}/payments")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PaymentDto>> getPayments(@PathVariable Long billId) {
        return ResponseEntity.ok(billingService.getPayments(billId));
    }

    @PostMapping("/{billId}/payments")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> recordPayment(@PathVariable Long billId, @Valid @RequestBody PaymentDto dto) {
        try {
            return ResponseEntity.status(201).body(billingService.recordPayment(billId, dto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }
}