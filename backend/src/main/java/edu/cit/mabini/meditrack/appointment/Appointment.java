package edu.cit.mabini.meditrack.appointment;

import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    public enum AppointmentStatus {
        REQUESTED,
        PENDING_APPROVAL,
        APPROVED,
        CHECKED_IN,
        WAITING,
        IN_CONSULTATION,
        PRESCRIPTION_ISSUED,
        COMPLETED,
        CANCELLED,
        REJECTED,
        NO_SHOW
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    private String appointmentNumber;

    private String appointmentType;

    private Integer queueNumber;

    private Integer durationMinutes;

    private Integer priority;

    private String notes;

    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.REQUESTED;

    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;

        if (this.status == null) {
            this.status = AppointmentStatus.REQUESTED;
        }

    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
