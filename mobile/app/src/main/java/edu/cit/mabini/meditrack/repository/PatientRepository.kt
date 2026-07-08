package edu.cit.mabini.meditrack.repository

import edu.cit.mabini.meditrack.api.ApiService
import edu.cit.mabini.meditrack.model.PatientDto
import retrofit2.Response

class PatientRepository(private val apiService: ApiService) {
    suspend fun getPatients(): Response<List<PatientDto>> = apiService.getPatients()
    suspend fun createPatient(patient: PatientDto): Response<PatientDto> = apiService.createPatient(patient)
    suspend fun updatePatient(id: Long, patient: PatientDto): Response<PatientDto> = apiService.updatePatient(id, patient)
    suspend fun deletePatient(id: Long): Response<Unit> = apiService.deletePatient(id)
}
