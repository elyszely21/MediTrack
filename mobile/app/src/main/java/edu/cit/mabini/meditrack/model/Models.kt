package edu.cit.mabini.meditrack.model

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

data class MedicalRecordDto(
    val id: Long? = null,
    val patientId: Long = 0,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val prescription: String? = null,
    val notes: String? = null,
    val visitDate: String? = null
)

data class AppointmentDto(
    val id: Long? = null,
    val patientId: Long = 0,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val status: String? = null,
    val remarks: String? = null
)
