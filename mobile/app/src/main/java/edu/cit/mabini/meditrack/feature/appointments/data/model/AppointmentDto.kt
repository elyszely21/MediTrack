package edu.cit.mabini.meditrack.feature.appointments.data.model

data class AppointmentDto(
    val id: Long? = null,
    val patientId: Long = 0,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val status: String? = null,
    val remarks: String? = null
)
