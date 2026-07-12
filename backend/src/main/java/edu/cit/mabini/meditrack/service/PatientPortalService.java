package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.AppointmentDto;
import edu.cit.mabini.meditrack.dto.MedicalRecordDto;
import edu.cit.mabini.meditrack.dto.PatientProfileDto;
import edu.cit.mabini.meditrack.dto.PrescriptionDto;
import edu.cit.mabini.meditrack.entity.Patient;
import edu.cit.mabini.meditrack.entity.User;
import edu.cit.mabini.meditrack.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientPortalService {

    private final PatientRepository     patientRepository;
    private final AppointmentService    appointmentService;
    private final PrescriptionService   prescriptionService;
    private final MedicalRecordService  medicalRecordService;

    // ── Resolve the caller's own patient record ─────────────────────────────

    private Patient resolvePatient(String email) {
        return patientRepository.findByUser_Email(email)
                .orElseThrow(() -> new IllegalArgumentException(
                    "No patient profile is linked to this account yet. " +
                    "Please contact the clinic to have your records connected."
                ));
    }

    // ── Profile ──────────────────────────────────────────────────────────────

    public PatientProfileDto getMyProfile(String email) {
        Patient patient = resolvePatient(email);
        User user = patient.getUser();

        return PatientProfileDto.builder()
                .id(patient.getId())
                .patientNumber(patient.getPatientNumber())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender())
                .address(patient.getAddress())
                .contactNumber(patient.getContactNumber())
                .emergencyContact(patient.getEmergencyContact())
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .build();
    }

    // ── Update the editable parts of my own profile ─────────────────────────

    public PatientProfileDto updateMyProfile(String email, PatientProfileDto dto) {
        Patient patient = resolvePatient(email);

        patient.setBirthDate(dto.getBirthDate());
        patient.setGender(dto.getGender());
        patient.setAddress(dto.getAddress());
        patient.setContactNumber(dto.getContactNumber());
        patient.setEmergencyContact(dto.getEmergencyContact());

        patientRepository.save(patient);
        return getMyProfile(email);
    }

    // ── My clinical data (read-only) ────────────────────────────────────────

    public List<AppointmentDto> getMyAppointments(String email) {
        return appointmentService.getAppointmentsByPatient(resolvePatient(email).getId());
    }

    public List<PrescriptionDto> getMyPrescriptions(String email) {
        return prescriptionService.getByPatient(resolvePatient(email).getId());
    }

    public List<MedicalRecordDto> getMyMedicalRecords(String email) {
        return medicalRecordService.getRecordsByPatient(resolvePatient(email).getId());
    }
}