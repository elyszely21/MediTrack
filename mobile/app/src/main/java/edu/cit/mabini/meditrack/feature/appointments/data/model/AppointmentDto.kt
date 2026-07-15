package edu.cit.mabini.meditrack.feature.appointments.data.model

data class AppointmentDto(
    val id: Long? = null,
    val patientId: Long = 0,
    val patientName: String? = null,
    val patientNumber: String? = null,
    val doctorId: Long? = null,
    val appointmentNumber: String? = null,
    val appointmentType: String = "CONSULTATION",
    val queueNumber: Int? = null,
    val durationMinutes: Int = 30,
    val priority: Int? = null,
    val notes: String? = null,
    val appointmentDate: String? = null,
    val appointmentTime: String? = null,
    val endTime: String? = null,
    val status: String? = null,
    val remarks: String? = null
)
