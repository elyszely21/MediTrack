package edu.cit.mabini.meditrack.model

data class PatientDto(
    val id: Long? = null,
    val patientNumber: String,
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val gender: String,
    val address: String,
    val contactNumber: String,
    val emergencyContact: String
)
