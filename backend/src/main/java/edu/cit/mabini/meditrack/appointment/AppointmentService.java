package edu.cit.mabini.meditrack.appointment;

import edu.cit.mabini.meditrack.common.audit.AuditLogService;
import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.patient.PatientLookupDto;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import edu.cit.mabini.meditrack.user.User;

import edu.cit.mabini.meditrack.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    // ── Scheduling constants (configurable later) ───────────────────────────────
    // Clinic hours: Monday–Friday, 8:00 AM–5:00 PM
    private static final int CLINIC_START_HOUR = 8;
    private static final int CLINIC_END_HOUR = 17; // 5:00 PM

    // Lunch break: 12:00 PM–1:00 PM (inclusive start, exclusive end)
    private static final int LUNCH_START_HOUR = 12;
    private static final int LUNCH_END_HOUR = 13;

    // Queue & appointment number formats
    private static final String APPOINTMENT_NUMBER_PREFIX = "APT";


    public List<AppointmentDto> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public PatientLookupDto lookupPatientByNumber(String patientNumber) {
        return patientRepository.findByPatientNumber(patientNumber)
                .map(this::toPatientLookupDto)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
    }


    public List<AppointmentDto> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDate(date)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<AppointmentDto> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<AppointmentDto> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.findByStatus(status)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public AppointmentDto createAppointment(AppointmentDto dto) {
        validateCreateOrUpdateRequest(dto, null);

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        User doctor = userRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        validateDoctorAvailability(dto, doctor);
        validateDoctorConflict(dto, doctor);

        int queueNumber = nextQueueNumber(dto.getAppointmentDate());
        String appointmentNumber = nextAppointmentNumber(dto.getAppointmentDate());



        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentNumber(appointmentNumber)
                .appointmentType(dto.getAppointmentType())
                .queueNumber(queueNumber)
                .durationMinutes(dto.getDurationMinutes())
                .priority(dto.getPriority())
                .notes(dto.getNotes())
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(dto.getAppointmentTime())
                .status(Appointment.AppointmentStatus.REQUESTED)

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


    public AppointmentDto updateAppointment(Long id, AppointmentDto dto) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));



        validateCreateOrUpdateRequest(dto, appointment);

        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setAppointmentTime(dto.getAppointmentTime());
        if (dto.getStatus() != null) appointment.setStatus(dto.getStatus());
        appointment.setRemarks(dto.getRemarks());

        appointment.setAppointmentNumber(dto.getAppointmentNumber());
        appointment.setAppointmentType(dto.getAppointmentType());
        appointment.setQueueNumber(dto.getQueueNumber());
        appointment.setDurationMinutes(dto.getDurationMinutes());
        appointment.setPriority(dto.getPriority());
        appointment.setNotes(dto.getNotes());

        User doctor = userRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        appointment.setDoctor(doctor);

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
                "UPDATED",
                "Appointment",
                String.valueOf(id),
                "Appointment updated — Status: " + saved.getStatus()
        );

        return toDto(saved);
    }


    public AppointmentDto approveAppointment(Long id) {
        // Keep existing API behavior ("approve" moves into APPROVED)
        return transitionToStatus(
                id,
                java.util.List.of(Appointment.AppointmentStatus.PENDING_APPROVAL),
                Appointment.AppointmentStatus.APPROVED,
                "APPROVE"
        );
    }

    private AppointmentDto transitionToStatus(
            Long id,
            java.util.List<Appointment.AppointmentStatus> fromStatuses,
            Appointment.AppointmentStatus toStatus,
            String action)
    {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new InvalidAppointmentRequestException("Appointment not found"));

        if (!fromStatuses.contains(appointment.getStatus())) {
            throw new InvalidAppointmentRequestException(
                    "Invalid status transition: " + appointment.getStatus() + " -> " + toStatus
            );
        }

        Appointment.AppointmentStatus previousStatus = appointment.getStatus();
        appointment.setStatus(toStatus);
        appointment.setUpdatedAt(LocalDateTime.now());


        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
                action,
                "Appointment",
                String.valueOf(id),
                "Appointment status updated: " + previousStatus + " -> " + toStatus
        );

        return toDto(saved);
    }



    // ── Appointment status workflow ─────────────────────────────────────────

    public AppointmentDto checkInAppointment(Long id) {
        return transitionToStatus(
                id,
                java.util.List.of(Appointment.AppointmentStatus.APPROVED),
                Appointment.AppointmentStatus.CHECKED_IN,
                "CHECKED_IN"
        );
    }


    public AppointmentDto waitingAppointment(Long id) {
        return transitionToStatus(
                id,
                java.util.List.of(Appointment.AppointmentStatus.CHECKED_IN),                    
                Appointment.AppointmentStatus.WAITING,
                "WAITING"
            );
        }

    public AppointmentDto startConsultationAppointment(Long id) {
        return transitionToStatus(
                id,
                java.util.List.of(Appointment.AppointmentStatus.WAITING),
                Appointment.AppointmentStatus.IN_CONSULTATION,
                "IN_CONSULTATION"
        );
    }

    public AppointmentDto issuePrescriptionAppointment(Long id) {
        return transitionToStatus(
                id,
                java.util.List.of(Appointment.AppointmentStatus.IN_CONSULTATION),
                Appointment.AppointmentStatus.PRESCRIPTION_ISSUED,
                "PRESCRIPTION_ISSUED"
        );
    }

    public AppointmentDto noShowAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // Valid: APPROVED or WAITING only
        if (appointment.getStatus() != Appointment.AppointmentStatus.APPROVED
                && appointment.getStatus() != Appointment.AppointmentStatus.WAITING) {
            throw new InvalidAppointmentRequestException(
                    "Invalid status transition: " + appointment.getStatus() + " -> NO_SHOW"
            );
        }

        appointment.setStatus(Appointment.AppointmentStatus.NO_SHOW);
        appointment.setUpdatedAt(java.time.LocalDateTime.now());

        if (reason != null && !reason.isBlank()) {
            appointment.setRemarks(reason);
        }

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
                "NO_SHOW",
                "Appointment",
                String.valueOf(id),
                "Appointment marked as NO_SHOW for: "
                        + saved.getPatient().getFirstName()
                        + " " + saved.getPatient().getLastName()
                        + (reason != null ? " — Reason: " + reason : "")
        );

        return toDto(saved);
    }







    public AppointmentDto rejectAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // Valid transitions:
        // REQUESTED -> REJECTED
        // PENDING_APPROVAL -> REJECTED
        // APPROVED -> REJECTED (not requested) so keep strict: only REQUESTED or PENDING_APPROVAL.
        if (appointment.getStatus() != Appointment.AppointmentStatus.REQUESTED
                && appointment.getStatus() != Appointment.AppointmentStatus.PENDING_APPROVAL) {
            throw new InvalidAppointmentRequestException(
                    "Invalid status transition: " + appointment.getStatus() + " -> REJECTED"
            );
        }

        appointment.setStatus(Appointment.AppointmentStatus.REJECTED);
        appointment.setUpdatedAt(java.time.LocalDateTime.now());

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


    public AppointmentDto completeAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // Valid transitions into COMPLETED:
        // IN_CONSULTATION -> COMPLETED
        // PRESCRIPTION_ISSUED -> COMPLETED
        if (appointment.getStatus() != Appointment.AppointmentStatus.IN_CONSULTATION
                && appointment.getStatus() != Appointment.AppointmentStatus.PRESCRIPTION_ISSUED) {
            throw new InvalidAppointmentRequestException(
                    "Invalid status transition: " + appointment.getStatus() + " -> COMPLETED"
            );
        }

        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointment.setUpdatedAt(java.time.LocalDateTime.now());

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


    public AppointmentDto cancelAppointment(Long id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // Valid transition: APPROVED -> CANCELLED
        if (appointment.getStatus() != Appointment.AppointmentStatus.APPROVED) {
            throw new InvalidAppointmentRequestException(
                    "Invalid status transition: " + appointment.getStatus() + " -> CANCELLED"
            );
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(java.time.LocalDateTime.now());
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

    private void validateCreateOrUpdateRequest(AppointmentDto dto, Appointment current)
    {

        if (dto.getPatientId() == null) {
            throw new InvalidAppointmentRequestException("patient is required");
        }
        if (dto.getDoctorId() == null) {
            throw new InvalidAppointmentRequestException("doctor is required");
        }
        if (dto.getAppointmentDate() == null) {
            throw new InvalidAppointmentRequestException("appointmentDate is required");
        }
        if (dto.getAppointmentTime() == null) {
            throw new InvalidAppointmentRequestException("startTime is required");
        }
        // endTime is computed in DTO responses only; entity uses appointmentTime + durationMinutes.
        // Keep backward compatibility with existing DTO contract by still validating endTime consistency.
        if (dto.getEndTime() == null) {
            throw new InvalidAppointmentRequestException("endTime is required");
        }

        if (dto.getAppointmentType() == null || dto.getAppointmentType().isBlank()) {
            throw new InvalidAppointmentRequestException("appointmentType is required");
        }
        if (dto.getRemarks() == null || dto.getRemarks().isBlank()) {
            throw new InvalidAppointmentRequestException("reason is required");
        }

        // Update restrictions
        if (current != null) {
            if (current.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
                throw new InvalidAppointmentRequestException("Completed appointments cannot be edited");
            }
            if (current.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
                throw new InvalidAppointmentRequestException("Cancelled appointments cannot be edited");
            }
            // "No Show" isn't present in current enum; treat unknown as not allowed to edit.
        }

        // Date validation
        var today = java.time.LocalDate.now();
        if (dto.getAppointmentDate().isBefore(today)) {
            throw new InvalidAppointmentRequestException("Appointment date cannot be in the past");
        }
        var maxDate = today.plusMonths(6);
        if (dto.getAppointmentDate().isAfter(maxDate)) {
            throw new InvalidAppointmentRequestException("Appointment cannot be scheduled more than 6 months in advance");
        }

        // Time validation
        if (!dto.getEndTime().isAfter(dto.getAppointmentTime())) {
            throw new InvalidAppointmentRequestException("endTime must be after startTime");
        }

        // durationMinutes must be greater than zero (also enforced by DTO @Positive)
        if (dto.getDurationMinutes() == null || dto.getDurationMinutes() <= 0) {
            throw new InvalidAppointmentRequestException("durationMinutes must be greater than zero");
        }

        // durationMinutes must match difference
        // durationMinutes must match difference between startTime and endTime (computed from entity)
        long minutesDiff = java.time.Duration.between(
                dto.getAppointmentTime().atDate(java.time.LocalDate.of(2000, 1, 1)),
                dto.getEndTime().atDate(java.time.LocalDate.of(2000, 1, 1))
        ).toMinutes();


        if (minutesDiff != dto.getDurationMinutes()) {
            throw new InvalidAppointmentRequestException(
                    "durationMinutes must match the difference between startTime and endTime"
            );
        }

        // Enum validation (status transitions are handled via update endpoints)
        if (dto.getStatus() != null) {
            try {
                Appointment.AppointmentStatus.valueOf(dto.getStatus().name());
            } catch (Exception ex) {
                throw new InvalidAppointmentRequestException("Invalid AppointmentStatus");
            }
        }
    }

    private void validateDoctorAvailability(AppointmentDto dto, User doctor) {
        java.time.DayOfWeek dow = dto.getAppointmentDate().getDayOfWeek();

        // Mon–Fri only
        if (dow == java.time.DayOfWeek.SATURDAY || dow == java.time.DayOfWeek.SUNDAY) {
            throw new InvalidAppointmentRequestException("Doctor appointments are only available Monday–Friday");
        }

        int startHour = dto.getAppointmentTime().getHour();
        int endHour = dto.getEndTime().getHour();
        int startMinute = dto.getAppointmentTime().getMinute();
        int endMinute = dto.getEndTime().getMinute();

        // Reject outside 8:00–17:00 (exclusive end: last possible end is 17:00)
        java.time.LocalTime openStart = java.time.LocalTime.of(CLINIC_START_HOUR, 0);
        java.time.LocalTime openEnd = java.time.LocalTime.of(CLINIC_END_HOUR, 0);

        java.time.LocalTime start = dto.getAppointmentTime();
        java.time.LocalTime end = dto.getEndTime();

        if (start.isBefore(openStart) || !end.isAfter(start) || end.isAfter(openEnd)) {
            throw new InvalidAppointmentRequestException("Appointment must be within clinic hours (8:00 AM–5:00 PM)");
        }

        // Lunch break rejection: 12:00–13:00 (exclusive end)
        java.time.LocalTime lunchStart = java.time.LocalTime.of(LUNCH_START_HOUR, 0);
        java.time.LocalTime lunchEnd = java.time.LocalTime.of(LUNCH_END_HOUR, 0);

        // Reject if overlap with lunch interval.
        boolean overlapsLunch = start.isBefore(lunchEnd) && end.isAfter(lunchStart);
        if (overlapsLunch) {
            throw new InvalidAppointmentRequestException("Appointment cannot be scheduled during the lunch break (12:00 PM–1:00 PM)");
        }
    }

    private void validateDoctorConflict(AppointmentDto dto, User doctor) {

        // Active appointments only: Ignore CANCELLED and REJECTED
        // Optimize with doctor + date scope.
        java.time.LocalDate newDate = dto.getAppointmentDate();
        java.time.LocalTime newStart = dto.getAppointmentTime();
        java.time.LocalTime newEnd = dto.getEndTime();

        List<Appointment> sameDoctorAppointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), newDate);

        for (Appointment existing : sameDoctorAppointments) {


            if (existing.getAppointmentDate() == null || !existing.getAppointmentDate().equals(newDate)) {
                continue;
            }
            if (existing.getId() != null && dto.getId() != null && existing.getId().equals(dto.getId())) {
                continue; // exclude the current appointment being updated
            }
            if (existing.getStatus() == Appointment.AppointmentStatus.CANCELLED || existing.getStatus() == Appointment.AppointmentStatus.REJECTED) {
                continue;
            }


            // Determine existingEnd using existing appointmentTime + existing durationMinutes
            java.time.LocalTime existingStart = existing.getAppointmentTime();
            Integer existingDuration = existing.getDurationMinutes();
            if (existingDuration == null) {
                // Fallback: if duration missing, treat as zero-length conflict (no overlap)
                continue;
            }
            java.time.LocalTime existingEnd = existingStart.plusMinutes(existingDuration.longValue());

            // Overlap rule: newStart < existingEnd AND newEnd > existingStart
            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                throw new InvalidAppointmentRequestException("Doctor already has an active appointment overlapping this time");
            }
        }
    }

    private int nextQueueNumber(java.time.LocalDate appointmentDate) {
        // Queue numbers restart every day.
        // Example: Queue 1..N for that day.
        long todayCount = appointmentRepository.countByAppointmentDate(appointmentDate);
        return (int) todayCount + 1;
    }

    private String nextAppointmentNumber(java.time.LocalDate appointmentDate) {
        long count = appointmentRepository.countByAppointmentDate(appointmentDate);
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = appointmentDate.format(fmt);
        long seq = count + 1;
        return APPOINTMENT_NUMBER_PREFIX + "-" + datePart + "-" + String.format("%04d", seq);
    }


    private PatientLookupDto toPatientLookupDto(Patient patient) {
        return PatientLookupDto.builder()
                .id(patient.getId())
                .patientNumber(patient.getPatientNumber())
                .fullName(patient.getFirstName() + " " + patient.getLastName())
                .build();
    }

    private AppointmentDto toDto(Appointment a) {


        LocalTime computedEndTime = null;

        if (a.getAppointmentTime() != null && a.getDurationMinutes() != null) {
            computedEndTime = a.getAppointmentTime()
                    .plusMinutes(a.getDurationMinutes().longValue());
        }

        return AppointmentDto.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .doctorId(a.getDoctor() != null ? a.getDoctor().getId() : null)
                .appointmentNumber(a.getAppointmentNumber())
                .appointmentType(a.getAppointmentType())
                .queueNumber(a.getQueueNumber())
                .durationMinutes(a.getDurationMinutes())
                .priority(a.getPriority())
                .notes(a.getNotes())
                .appointmentDate(a.getAppointmentDate())
                .appointmentTime(a.getAppointmentTime())
                .endTime(computedEndTime)
                .status(a.getStatus())
                .remarks(a.getRemarks())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

}


