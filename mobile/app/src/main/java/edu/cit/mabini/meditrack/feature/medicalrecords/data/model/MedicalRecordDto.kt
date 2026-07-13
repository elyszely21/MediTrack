package edu.cit.mabini.meditrack.feature.medicalrecords.data.model

data class MedicalRecordDto(
    val id: Long? = null,
    val patientId: Long = 0,
    val diagnosis: String? = null,
    val treatment: String? = null,
    val prescription: String? = null,
    val notes: String? = null,
    val visitDate: String? = null
)
