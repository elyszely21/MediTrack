package edu.cit.mabini.meditrack.repository;

import edu.cit.mabini.meditrack.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientNumber(String patientNumber);
    boolean existsByPatientNumber(String patientNumber);
    Optional<Patient> findByEmail(String email);
    boolean existsByEmail(String email);
}