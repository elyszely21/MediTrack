package edu.cit.mabini.meditrack.patient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Login credentials live directly on Patient now — a self-registered
    // patient is their own account, not a row in the staff "users" table.
    // Both are nullable because patients added manually by staff (walk-ins,
    // phone bookings) have no portal login at all.
    @Column(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String patientNumber;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private LocalDate birthDate;

    private String gender;

    private String address;

    private String contactNumber;

    private String emergencyContact;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}