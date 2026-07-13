package edu.cit.mabini.meditrack.security;

import edu.cit.mabini.meditrack.patient.Patient;
import edu.cit.mabini.meditrack.user.User;
import edu.cit.mabini.meditrack.patient.PatientRepository;
import edu.cit.mabini.meditrack.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository    userRepository;
    private final PatientRepository patientRepository;

    // Two separate account tables now: staff logins live in "users"
    // (SUPER_ADMIN / NURSE / DOCTOR), patient logins live directly on
    // "patients". A given email can only exist in one of the two —
    // enforced at registration time — so checking users first and
    // falling back to patients is unambiguous.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = email.trim().toLowerCase();

        return userRepository.findByEmail(normalized)
                .map(this::fromUser)
                .or(() -> patientRepository.findByEmail(normalized)
                    .filter(p -> p.getPassword() != null)
                    .map(this::fromPatient))
                .orElseThrow(() ->
                    new UsernameNotFoundException("No account found with email: " + email));
    }

    private CustomUserDetails fromUser(User user) {
        return new CustomUserDetails(
            user.getId(), user.getEmail(), user.getPassword(), user.getRole(), user.getFullName()
        );
    }

    private CustomUserDetails fromPatient(Patient patient) {
        String fullName = (patient.getFirstName() + " " + patient.getLastName()).trim();
        return new CustomUserDetails(
            patient.getId(), patient.getEmail(), patient.getPassword(), "PATIENT", fullName
        );
    }
}