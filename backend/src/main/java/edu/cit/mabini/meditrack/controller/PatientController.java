package edu.cit.mabini.meditrack.controller;

import edu.cit.mabini.meditrack.dto.PatientDto;
import edu.cit.mabini.meditrack.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    // ── Get all active patients ───────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<PatientDto>> getPatients() {
        return ResponseEntity.ok(patientService.findAllPatients());
    }

    // ── Get all archived patients ─────────────────────────────────────────────

    @GetMapping("/archived")
    public ResponseEntity<List<PatientDto>> getArchivedPatients() {
        return ResponseEntity.ok(patientService.findArchivedPatients());
    }

    // ── Search patients ───────────────────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<List<PatientDto>> searchPatients(
            @RequestParam String q,
            @RequestParam(defaultValue = "false") boolean archived) {
        return ResponseEntity.ok(patientService.searchPatients(q, archived));
    }

    // ── Get single patient ────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatient(id));
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<PatientDto> createPatient(
            @Valid @RequestBody PatientDto dto) {
        return ResponseEntity.status(201).body(patientService.createPatient(dto));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientDto dto) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    // ── Archive ───────────────────────────────────────────────────────────────

    @PutMapping("/{id}/archive")
    public ResponseEntity<PatientDto> archivePatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.archivePatient(id));
    }

    // ── Unarchive ─────────────────────────────────────────────────────────────

    @PutMapping("/{id}/unarchive")
    public ResponseEntity<PatientDto> unarchivePatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.unarchivePatient(id));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}