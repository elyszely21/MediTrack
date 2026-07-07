package edu.cit.mabini.meditrack.service;

import edu.cit.mabini.meditrack.dto.LoginRequest;
import edu.cit.mabini.meditrack.dto.LoginResponse;
import edu.cit.mabini.meditrack.dto.RegisterRequest;
import edu.cit.mabini.meditrack.entity.User;
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

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse register(RegisterRequest request) {
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

    public LoginResponse login(LoginRequest request) {
        User user = userService.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
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
}
