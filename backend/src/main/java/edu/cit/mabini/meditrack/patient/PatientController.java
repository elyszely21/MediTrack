package edu.cit.mabini.meditrack.patient;

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

    @GetMapping
    public ResponseEntity<List<PatientDto>> getPatients() {
        return ResponseEntity.ok(patientService.findAllPatients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatient(id));
    }

    @GetMapping("/archived")
    public ResponseEntity<List<PatientDto>> getArchivedPatients() {
        return ResponseEntity.ok(patientService.findArchivedPatients());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientDto>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "false") boolean archived) {
        return ResponseEntity.ok(patientService.searchPatients(q, archived));
    }

    @GetMapping("/by-number/{patientNumber}")
    public ResponseEntity<PatientDto> getByPatientNumber(
            @PathVariable String patientNumber) {
        return ResponseEntity.ok(patientService.getPatientByNumber(patientNumber));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<PatientDto> archive(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.archivePatient(id));
    }

    @PutMapping("/{id}/unarchive")
    public ResponseEntity<PatientDto> unarchive(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.unarchivePatient(id));
    }

    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientDto dto) {
        return ResponseEntity.status(201).body(patientService.createPatient(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientDto dto) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}