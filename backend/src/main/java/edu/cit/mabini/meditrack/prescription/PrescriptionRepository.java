package edu.cit.mabini.meditrack.prescription;

import edu.cit.mabini.meditrack.prescription.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientId(Long patientId);
    List<Prescription> findByMedicalRecordId(Long medicalRecordId);
}