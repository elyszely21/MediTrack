package edu.cit.mabini.meditrack.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientNumber(String patientNumber);
    boolean existsByPatientNumber(String patientNumber);
    Optional<Patient> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Patient> findByArchivedFalse();
    List<Patient> findByArchivedTrue();

    @Query("SELECT p FROM Patient p WHERE p.archived = :archived AND ("
         + "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :q, '%')) "
         + "OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :q, '%')) "
         + "OR LOWER(p.patientNumber) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Patient> search(@Param("q") String q, @Param("archived") boolean archived);
}