package edu.cit.mabini.meditrack.controller;

import edu.cit.mabini.meditrack.dto.LoginRequest;
import edu.cit.mabini.meditrack.dto.LoginResponse;
import edu.cit.mabini.meditrack.dto.RegisterRequest;
import edu.cit.mabini.meditrack.entity.User;
import edu.cit.mabini.meditrack.service.AuthenticationService;
import edu.cit.mabini.meditrack.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authenticationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile() {
        return ResponseEntity.ok(Map.of("message", "Authenticated user profile endpoint"));
    }

    @PostMapping("/admin/register-nurse")
    public ResponseEntity<?> registerNurse(@Valid @RequestBody RegisterRequest request, Authentication authentication) {
        try {
            // Check if user is SUPER_ADMIN
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
            }

            boolean isSuperAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("ROLE_SUPER_ADMIN"));

            if (!isSuperAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only SUPER_ADMIN can register nurses"));
            }

            // Register as NURSE
            request.setRole("NURSE");
            LoginResponse response = authenticationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }
}
