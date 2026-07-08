package edu.cit.mabini.meditrack.model

data class AppointmentDto(
    val id: Long? = null,
    val patientId: Long,
    val appointmentDate: String,
    val appointmentTime: String,
    val status: String,
    val remarks: String
)
