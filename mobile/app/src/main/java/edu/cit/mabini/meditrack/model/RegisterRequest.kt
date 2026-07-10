package edu.cit.mabini.meditrack.model

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val confirmPassword: String
)
