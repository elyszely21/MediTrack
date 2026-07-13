package edu.cit.mabini.meditrack.common.audit;

import edu.cit.mabini.meditrack.appointment.Appointment;

import edu.cit.mabini.meditrack.patient.Patient;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;       // e.g. "CREATED", "UPDATED", "DELETED"

    @Column(nullable = false)
    private String entity;       // e.g. "Patient", "Appointment"

    private String entityId;     // ID of the affected record

    private String performedBy;  // email of the user who did it

    @Column(columnDefinition = "TEXT")
    private String details;      // optional extra info

    @Column(nullable = false, updatable = false)
    private LocalDateTime performedAt;

    @PrePersist
    public void prePersist() {
        this.performedAt = LocalDateTime.now();
    }
}