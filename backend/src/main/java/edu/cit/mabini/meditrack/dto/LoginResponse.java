package edu.cit.mabini.meditrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String message;
    private String fullName;
    private String email;
    private String role;
}
