package edu.cit.mabini.meditrack.appointment;

import edu.cit.mabini.meditrack.appointment.AppointmentDto;
import edu.cit.mabini.meditrack.appointment.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(
            appointmentService.getAppointmentsByStatus(status));
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

    // ── Delete ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}