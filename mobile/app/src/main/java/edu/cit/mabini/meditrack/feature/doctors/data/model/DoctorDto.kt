package edu.cit.mabini.meditrack.feature.doctors.data.model

data class DoctorDto(
    val id: Long,
    val fullName: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val specialization: String? = null,
    val licenseNumber: String? = null,
    val role: String? = null
)
