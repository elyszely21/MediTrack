package edu.cit.mabini.meditrack.model

data class MedicalRecordDto(
    val id: Long? = null,
    val patientId: Long,
    val diagnosis: String,
    val treatment: String,
    val prescription: String,
    val notes: String,
    val visitDate: String
)
