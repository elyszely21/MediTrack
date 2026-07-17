package edu.cit.mabini.meditrack.patient;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PatientDto>> getPatients() {
        return ResponseEntity.ok(patientService.findAllPatients());
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PatientDto> getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatient(id));
    }


    @GetMapping("/archived")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PatientDto>> getArchivedPatients() {
        return ResponseEntity.ok(patientService.findArchivedPatients());
    }


    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PatientDto>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "false") boolean archived) {
        return ResponseEntity.ok(patientService.searchPatients(q, archived));
    }


    @GetMapping("/by-number/{patientNumber}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PatientDto> getByPatientNumber(
            @PathVariable String patientNumber) {
        return ResponseEntity.ok(patientService.getPatientByNumber(patientNumber));
    }


    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PatientDto> archive(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.archivePatient(id));
    }


    @PutMapping("/{id}/unarchive")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PatientDto> unarchive(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.unarchivePatient(id));
    }


    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientDto dto) {
        return ResponseEntity.status(201).body(patientService.createPatient(dto));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientDto dto) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/staff-lookup")
    @PreAuthorize("hasAnyRole('DOCTOR','NURSE','SUPER_ADMIN')")
    public ResponseEntity<List<PatientDto>> staffLookup(
            Authentication authentication,
            @RequestParam(required = false) String q) {
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .map(s -> s != null && s.startsWith("ROLE_") ? s.substring("ROLE_".length()) : s)
                .orElse("");
        Long staffId = authentication.getPrincipal() instanceof edu.cit.mabini.meditrack.security.CustomUserDetails cud
                ? cud.getId() : null;
        return ResponseEntity.ok(patientService.findPatientsForStaff(role, staffId, q));
    }

}