package edu.cit.mabini.meditrack.repository

import edu.cit.mabini.meditrack.api.ApiService
import edu.cit.mabini.meditrack.model.MedicalRecordDto
import retrofit2.Response

class MedicalRecordRepository(private val apiService: ApiService) {
    suspend fun getMedicalRecords(): Response<List<MedicalRecordDto>> = apiService.getMedicalRecords()
    suspend fun createMedicalRecord(record: MedicalRecordDto): Response<MedicalRecordDto> = apiService.createMedicalRecord(record)
    suspend fun updateMedicalRecord(id: Long, record: MedicalRecordDto): Response<MedicalRecordDto> = apiService.updateMedicalRecord(id, record)
    suspend fun deleteMedicalRecord(id: Long): Response<Unit> = apiService.deleteMedicalRecord(id)
}
