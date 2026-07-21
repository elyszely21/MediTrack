package edu.cit.mabini.meditrack.feature.patients

import edu.cit.mabini.meditrack.core.network.ApiService
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientDto
import retrofit2.Response

class PatientRepository(private val api: ApiService) {
    suspend fun getAll(): Response<List<PatientDto>> = api.getPatients()
    suspend fun getArchived(): Response<List<PatientDto>> = api.getArchivedPatients()
    suspend fun staffLookup(query: String? = null): Response<List<PatientDto>> = api.getStaffLookupPatients(query)
    suspend fun search(query: String, archived: Boolean = false): Response<List<PatientDto>> = 
        api.searchPatients(query, archived)

    suspend fun getById(id: Long): Response<PatientDto> = api.getPatient(id)
    suspend fun create(dto: PatientDto): Response<PatientDto> = api.createPatient(dto)
    suspend fun update(id: Long, dto: PatientDto): Response<PatientDto> = api.updatePatient(id, dto)
    suspend fun archive(id: Long): Response<Unit> = api.archivePatient(id)
    suspend fun unarchive(id: Long): Response<Unit> = api.unarchivePatient(id)
    suspend fun delete(id: Long): Response<Unit> = api.deletePatient(id)
}
