package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.AppointmentDto;
import edu.cit.mabini.meditrack.entity.Appointment;
import edu.cit.mabini.meditrack.entity.Patient;
import edu.cit.mabini.meditrack.repository.AppointmentRepository;
import edu.cit.mabini.meditrack.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository     patientRepository;
    private final AuditLogService       auditLogService;

    // ── Get all ───────────────────────────────────────────────────────────────

    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // ── Get by date ───────────────────────────────────────────────────────────

    public List<AppointmentDto> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // ── Get by patient ────────────────────────────────────────────────────────

    public List<AppointmentDto> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // ── Get by status ─────────────────────────────────────────────────────────

    public List<AppointmentDto> getAppointmentsByStatus(String status) {
        return appointmentRepository.findByStatus(status)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // ── Create ────────────────────────────────────────────────────────────────

    public AppointmentDto createAppointment(AppointmentDto dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(dto.getAppointmentTime())
                .status("PENDING")
                .remarks(dto.getRemarks())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
            "CREATED",
            "Appointment",
            String.valueOf(saved.getId()),
            "Appointment scheduled for: "
                + patient.getFirstName() + " " + patient.getLastName()
                + " on " + saved.getAppointmentDate()
        );

        return toDto(saved);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public AppointmentDto updateAppointment(Long id, AppointmentDto dto) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Appointment not found"));

        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setAppointmentTime(dto.getAppointmentTime());
        appointment.setStatus(dto.getStatus());
        appointment.setRemarks(dto.getRemarks());

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
            "UPDATED",
            "Appointment",
            String.valueOf(id),
            "Appointment updated — Status: " + saved.getStatus()
        );

        return toDto(saved);
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    public AppointmentDto approveAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Appointment not found"));

        if (appointment.getStatus().equals("CANCELLED")) {
            throw new IllegalArgumentException(
                "Cannot approve a cancelled appointment");
        }

        appointment.setStatus("APPROVED");
        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
            "APPROVED",
            "Appointment",
            String.valueOf(id),
            "Appointment approved for: "
                + saved.getPatient().getFirstName()
                + " " + saved.getPatient().getLastName()
                + " on " + saved.getAppointmentDate()
        );

        return toDto(saved);
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    public AppointmentDto rejectAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Appointment not found"));

        appointment.setStatus("REJECTED");
        if (reason != null && !reason.isBlank()) {
            appointment.setRemarks(reason);
        }

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
            "REJECTED",
            "Appointment",
            String.valueOf(id),
            "Appointment rejected for: "
                + saved.getPatient().getFirstName()
                + " " + saved.getPatient().getLastName()
                + (reason != null ? " — Reason: " + reason : "")
        );

        return toDto(saved);
    }

    // ── Complete ──────────────────────────────────────────────────────────────

    public AppointmentDto completeAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Appointment not found"));

        if (!appointment.getStatus().equals("APPROVED")) {
            throw new IllegalArgumentException(
                "Only approved appointments can be marked as completed");
        }

        appointment.setStatus("COMPLETED");
        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
            "COMPLETED",
            "Appointment",
            String.valueOf(id),
            "Appointment completed for: "
                + saved.getPatient().getFirstName()
                + " " + saved.getPatient().getLastName()
        );

        return toDto(saved);
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    public AppointmentDto cancelAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Appointment not found"));

        if (appointment.getStatus().equals("COMPLETED")) {
            throw new IllegalArgumentException(
                "Cannot cancel a completed appointment");
        }

        appointment.setStatus("CANCELLED");
        if (reason != null && !reason.isBlank()) {
            appointment.setRemarks(reason);
        }

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
            "CANCELLED",
            "Appointment",
            String.valueOf(id),
            "Appointment cancelled for: "
                + saved.getPatient().getFirstName()
                + " " + saved.getPatient().getLastName()
                + (reason != null ? " — Reason: " + reason : "")
        );

        return toDto(saved);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Appointment not found");
        }

        auditLogService.log(
            "DELETED",
            "Appointment",
            String.valueOf(id),
            "Appointment removed"
        );

        appointmentRepository.deleteById(id);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private AppointmentDto toDto(Appointment a) {
        return AppointmentDto.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(
                    a.getPatient().getFirstName()
                    + " " + a.getPatient().getLastName()
                )
                .patientNumber(a.getPatient().getPatientNumber())
                .appointmentDate(a.getAppointmentDate())
                .appointmentTime(a.getAppointmentTime())
                .status(a.getStatus())
                .remarks(a.getRemarks())
                .createdAt(a.getCreatedAt())
                .build();
    }
}