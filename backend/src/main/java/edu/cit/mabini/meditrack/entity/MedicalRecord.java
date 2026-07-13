package edu.cit.mabini.meditrack.medicalrecord;

import edu.cit.mabini.meditrack.patient.Patient;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String diagnosis;

    private String treatment;

    private String prescription;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDate visitDate;
}
