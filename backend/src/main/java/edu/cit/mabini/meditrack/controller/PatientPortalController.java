package edu.cit.mabini.meditrack.controller;

import edu.cit.mabini.meditrack.dto.PatientProfileDto;
import edu.cit.mabini.meditrack.service.PatientPortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient-portal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PATIENT')")
public class PatientPortalController {

    private final PatientPortalService patientPortalService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        try {
            return ResponseEntity.ok(patientPortalService.getMyProfile(authentication.getName()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            @Valid @RequestBody PatientProfileDto dto,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(
                patientPortalService.updateMyProfile(authentication.getName(), dto)
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getMyAppointments(Authentication authentication) {
        try {
            return ResponseEntity.ok(patientPortalService.getMyAppointments(authentication.getName()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<?> getMyPrescriptions(Authentication authentication) {
        try {
            return ResponseEntity.ok(patientPortalService.getMyPrescriptions(authentication.getName()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/medical-records")
    public ResponseEntity<?> getMyMedicalRecords(Authentication authentication) {
        try {
            return ResponseEntity.ok(patientPortalService.getMyMedicalRecords(authentication.getName()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(java.util.Map.of("message", ex.getMessage()));
        }
    }
}