package edu.cit.mabini.meditrack.controller;

import edu.cit.mabini.meditrack.dto.LoginRequest;
import edu.cit.mabini.meditrack.dto.LoginResponse;
import edu.cit.mabini.meditrack.dto.RegisterRequest;
import edu.cit.mabini.meditrack.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    // ── Public register — always creates a Patient, never a User ───────────

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authenticationService.registerPatient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<?> profile() {
        return ResponseEntity.ok(
            Map.of("message", "Authenticated user profile endpoint")
        );
    }

    // ── Register Nurse (SUPER_ADMIN only) — creates a staff User ────────────

    @PostMapping("/admin/register-nurse")
    public ResponseEntity<?> registerNurse(
            @Valid @RequestBody RegisterRequest request,
            Authentication authentication) {
        try {
            if (!isSuperAdmin(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Only SUPER_ADMIN can register nurses"));
            }

            LoginResponse response = authenticationService.registerStaff(request, "NURSE");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // ── Register Doctor (SUPER_ADMIN only) — creates a staff User ───────────

    @PostMapping("/admin/register-doctor")
    public ResponseEntity<?> registerDoctor(
            @Valid @RequestBody RegisterRequest request,
            Authentication authentication) {
        try {
            if (!isSuperAdmin(authentication)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Only SUPER_ADMIN can register doctors"));
            }

            LoginResponse response = authenticationService.registerStaff(request, "DOCTOR");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private boolean isSuperAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_SUPER_ADMIN"));
    }
}