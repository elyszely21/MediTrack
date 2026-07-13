package edu.cit.mabini.meditrack.consultation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ConsultationDto>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(consultationService.getByPatient(patientId));
    }

    @PostMapping
    public ResponseEntity<ConsultationDto> create(@Valid @RequestBody ConsultationDto dto) {
        return ResponseEntity.status(201).body(consultationService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultationDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationDto dto) {
        return ResponseEntity.ok(consultationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        consultationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}