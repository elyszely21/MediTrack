package edu.cit.mabini.meditrack.repository;

import edu.cit.mabini.meditrack.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentDate(LocalDate appointmentDate);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByStatus(String status);
    long countByStatus(String status);
}