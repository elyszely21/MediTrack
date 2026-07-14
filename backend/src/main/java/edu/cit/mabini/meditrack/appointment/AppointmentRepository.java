package edu.cit.mabini.meditrack.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentDate(LocalDate appointmentDate);
    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

    long countByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

    long countByAppointmentDate(LocalDate appointmentDate);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    long countByStatus(Appointment.AppointmentStatus status);



}

