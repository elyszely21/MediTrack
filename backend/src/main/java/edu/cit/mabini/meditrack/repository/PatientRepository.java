package edu.cit.mabini.meditrack.repository;

import edu.cit.mabini.meditrack.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByPatientNumber(String patientNumber);

    // All active patients
    List<Patient> findByArchivedFalse();

    // All archived patients
    List<Patient> findByArchivedTrue();

    // Search active patients by name or patient number
    @Query("""
        SELECT p FROM Patient p
        WHERE p.archived = false
        AND (
            LOWER(p.firstName)     LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(p.lastName)      LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(p.patientNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(CONCAT(p.firstName, ' ', p.lastName))
                                   LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    List<Patient> searchActivePatients(@Param("q") String q);

    // Search archived patients
    @Query("""
        SELECT p FROM Patient p
        WHERE p.archived = true
        AND (
            LOWER(p.firstName)     LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(p.lastName)      LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(p.patientNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(CONCAT(p.firstName, ' ', p.lastName))
                                   LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    List<Patient> searchArchivedPatients(@Param("q") String q);
}