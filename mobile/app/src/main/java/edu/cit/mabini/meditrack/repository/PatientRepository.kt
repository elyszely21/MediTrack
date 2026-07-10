package edu.cit.mabini.meditrack.repository

import edu.cit.mabini.meditrack.api.ApiService
import edu.cit.mabini.meditrack.model.PatientDto
import retrofit2.Response

class PatientRepository(private val api: ApiService) {
    suspend fun getAll(): Response<List<PatientDto>> = api.getPatients()
    suspend fun getById(id: Long): Response<PatientDto> = api.getPatient(id)
    suspend fun create(dto: PatientDto): Response<PatientDto> = api.createPatient(dto)
    suspend fun update(id: Long, dto: PatientDto): Response<PatientDto> = api.updatePatient(id, dto)
    suspend fun delete(id: Long): Response<Unit> = api.deletePatient(id)
}
