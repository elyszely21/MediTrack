package edu.cit.mabini.meditrack.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNurseRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    private String licenseNumber;

    private String department;

    private String specialization;

    // password update is optional
    private String password;

    private String confirmPassword;
}

