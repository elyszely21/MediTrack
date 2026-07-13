package edu.cit.mabini.meditrack.medicalrecord;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecordDto>> getRecords(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getRecordsByPatient(patientId));
    }

    @PostMapping
    public ResponseEntity<MedicalRecordDto> createRecord(@Valid @RequestBody MedicalRecordDto dto) {
        return ResponseEntity.status(201).body(medicalRecordService.createRecord(dto));
    }
}
