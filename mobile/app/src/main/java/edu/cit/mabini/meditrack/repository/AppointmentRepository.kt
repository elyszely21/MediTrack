package edu.cit.mabini.meditrack.repository

import edu.cit.mabini.meditrack.api.ApiService
import edu.cit.mabini.meditrack.model.AppointmentDto
import retrofit2.Response

class AppointmentRepository(private val api: ApiService) {
    suspend fun getAll(): Response<List<AppointmentDto>> = api.getAppointments()

    suspend fun getByDate(date: String): Response<List<AppointmentDto>> =
        api.getAppointmentsByDate(date)

    suspend fun getByPatient(id: Long): Response<List<AppointmentDto>> =
        api.getAppointmentsByPatient(id)

    suspend fun create(dto: AppointmentDto): Response<AppointmentDto> =
        api.createAppointment(dto)

    suspend fun update(id: Long, dto: AppointmentDto): Response<AppointmentDto> =
        api.updateAppointment(id, dto)

    suspend fun delete(id: Long): Response<Unit> = api.deleteAppointment(id)
}
