package edu.cit.mabini.meditrack.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterNurseRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String contactNumber;

    private String licenseNumber;

    private String department;

    private String specialization;

    private String password;

    private String confirmPassword;
}

