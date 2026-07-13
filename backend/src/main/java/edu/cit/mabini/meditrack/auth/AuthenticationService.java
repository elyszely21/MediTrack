package edu.cit.mabini.meditrack.auth;

import edu.cit.mabini.meditrack.patient.PatientService;

import edu.cit.mabini.meditrack.user.UserService;

import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.user.User;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import edu.cit.mabini.meditrack.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService        userService;
    private final PatientService     patientService;
    private final PatientRepository  patientRepository;
    private final PasswordEncoder    passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider    jwtTokenProvider;

    // ── Public registration → always creates a Patient ──────────────────────

    public LoginResponse registerPatient(RegisterRequest request) {
        Patient patient = patientService.registerSelf(request);
        String fullName = (patient.getFirstName() + " " + patient.getLastName()).trim();

        return LoginResponse.builder()
                .success(true)
                .message("Registration successful")
                .fullName(fullName)
                .email(patient.getEmail())
                .role("PATIENT")
                .token(jwtTokenProvider.generateToken(patient.getEmail(), "PATIENT"))
                .build();
    }

    // ── Admin-triggered staff registration → creates a User ─────────────────

    public LoginResponse registerStaff(RegisterRequest request, String role) {
        request.setRole(role);
        User user = userService.register(request);

        return LoginResponse.builder()
                .success(true)
                .message("Registration successful")
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(jwtTokenProvider.generateToken(user.getEmail(), user.getRole()))
                .build();
    }

    // ── Login — checks staff accounts first, then patient accounts ─────────

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        var staffUser = userService.findUserByEmail(email);
        if (staffUser.isPresent()) {
            return loginAsStaff(staffUser.get(), email, request.getPassword());
        }

        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        return loginAsPatient(patient, email, request.getPassword());
    }

    private LoginResponse loginAsStaff(User user, String email, String rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, rawPassword)
        );

        return LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(jwtTokenProvider.generateToken(authentication))
                .build();
    }

    private LoginResponse loginAsPatient(Patient patient, String email, String rawPassword) {
        if (patient.getPassword() == null) {
            throw new IllegalArgumentException(
                "This patient record has no portal login. Please register or contact the clinic."
            );
        }

        if (!passwordEncoder.matches(rawPassword, patient.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, rawPassword)
        );

        String fullName = (patient.getFirstName() + " " + patient.getLastName()).trim();

        return LoginResponse.builder()
                .success(true)
                .message("Login successful")
                .fullName(fullName)
                .email(patient.getEmail())
                .role("PATIENT")
                .token(jwtTokenProvider.generateToken(authentication))
                .build();
    }
}