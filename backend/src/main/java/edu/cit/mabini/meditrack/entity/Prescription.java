package edu.cit.mabini.meditrack.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @Column(nullable = false)
    private String medication;

    private String dosage;

    private String frequency;

    private String duration;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(nullable = false)
    private String prescribedBy;

    @Column(nullable = false)
    private LocalDate prescribedDate;

    @Builder.Default
    private String status = "ACTIVE";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        }
    }
}