package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.LoginRequest;
import edu.cit.mabini.meditrack.dto.LoginResponse;
import edu.cit.mabini.meditrack.dto.RegisterRequest;
import edu.cit.mabini.meditrack.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse register(RegisterRequest request) {
        User user = userService.register(request);
        return LoginResponse.builder()
                .message("Registration successful")
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        User user = userService.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        return LoginResponse.builder()
                .message("Login successful")
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
