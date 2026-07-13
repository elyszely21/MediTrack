package edu.cit.mabini.meditrack.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private String fullName;
    private String email;
    private String role;
    private String token;
}
