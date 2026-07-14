package edu.cit.mabini.meditrack.appointment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.mabini.meditrack.patient.PatientLookupDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ── Get all ───────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<AppointmentDto>> getAll() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    // ── Get by date ───────────────────────────────────────────────────────────

    @GetMapping("/date/{appointmentDate}")
    public ResponseEntity<List<AppointmentDto>> getByDate(
            @PathVariable LocalDate appointmentDate) {
        return ResponseEntity.ok(
            appointmentService.getAppointmentsByDate(appointmentDate));
    }

    // ── Get by patient ────────────────────────────────────────────────────────

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentDto>> getByPatient(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
            appointmentService.getAppointmentsByPatient(patientId));
    }

    // ── Get by status ─────────────────────────────────────────────────────────

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AppointmentDto>> getByStatus(
            @PathVariable String status) {
        Appointment.AppointmentStatus enumStatus;
        try {
            enumStatus = Appointment.AppointmentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(List.of());
        }
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(enumStatus));
    }


    // ── Create ────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<AppointmentDto> create(
            @Valid @RequestBody AppointmentDto dto) {
        return ResponseEntity.status(201)
                .body(appointmentService.createAppointment(dto));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentDto dto) {
        return ResponseEntity.ok(
            appointmentService.updateAppointment(id, dto));
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.approveAppointment(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String reason = body != null ? body.get("reason") : null;
            return ResponseEntity.ok(
                appointmentService.rejectAppointment(id, reason));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // ── Complete ──────────────────────────────────────────────────────────────

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(
                appointmentService.completeAppointment(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String reason = body != null ? body.get("reason") : null;
            return ResponseEntity.ok(
                appointmentService.cancelAppointment(id, reason));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // ── Appointment workflow ─────────────────────────────────────────────────

    @PutMapping("/{id}/check-in")
    public ResponseEntity<?> checkIn(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.checkInAppointment(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}/waiting")
    public ResponseEntity<?> waiting(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.waitingAppointment(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}/in-consultation")
    public ResponseEntity<?> inConsultation(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.startConsultationAppointment(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}/prescription-issued")
    public ResponseEntity<?> prescriptionIssued(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(appointmentService.issuePrescriptionAppointment(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/{id}/no-show")
    public ResponseEntity<?> noShow(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String reason = body != null ? body.get("reason") : null;
            return ResponseEntity.ok(appointmentService.noShowAppointment(id, reason));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @GetMapping("/lookup-patient/{patientNumber}")
    public ResponseEntity<?> lookupPatientByNumber(@PathVariable String patientNumber) {
        try {
            PatientLookupDto dto = appointmentService.lookupPatientByNumber(patientNumber);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            if ("Patient not found".equals(ex.getMessage())) {
                return ResponseEntity.status(404).body(Map.of("message", "Patient not found"));
            }
            return ResponseEntity.status(500).body(Map.of("message", "Unexpected server error"));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("message", "Unexpected server error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

}

