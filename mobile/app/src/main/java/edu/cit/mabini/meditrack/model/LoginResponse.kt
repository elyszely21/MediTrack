package edu.cit.mabini.meditrack.feature.auth

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val id: Long = 0, // Added User ID
    val fullName: String,
    val email: String,
    val role: String,
    val token: String
)
