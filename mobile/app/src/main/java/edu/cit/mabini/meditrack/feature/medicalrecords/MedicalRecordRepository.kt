package edu.cit.mabini.meditrack.feature.medicalrecords

import edu.cit.mabini.meditrack.core.network.ApiService
import edu.cit.mabini.meditrack.feature.medicalrecords.data.model.MedicalRecordDto
import retrofit2.Response

class MedicalRecordRepository(private val api: ApiService) {
    suspend fun getByPatient(patientId: Long): Response<List<MedicalRecordDto>> =
        api.getRecordsByPatient(patientId)

    suspend fun getMyRecords(): Response<List<MedicalRecordDto>> =
        api.getMyRecords()

    suspend fun create(dto: MedicalRecordDto): Response<MedicalRecordDto> =
        api.createRecord(dto)
}
