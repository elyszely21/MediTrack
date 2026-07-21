package edu.cit.mabini.meditrack.feature.auth

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val confirmPassword: String,
    val role: String? = null
)
