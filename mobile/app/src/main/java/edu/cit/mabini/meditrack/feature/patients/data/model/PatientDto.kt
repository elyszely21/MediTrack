package edu.cit.mabini.meditrack.feature.patients.data.model

data class PatientDto(
    val id: Long? = null,
    val patientNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val contactNumber: String? = null,
    val emergencyContact: String? = null
) {
    val fullName get() = "$firstName $lastName"
}
