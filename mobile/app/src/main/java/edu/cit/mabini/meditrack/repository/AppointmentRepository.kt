package edu.cit.mabini.meditrack.repository

import edu.cit.mabini.meditrack.api.ApiService
import edu.cit.mabini.meditrack.model.AppointmentDto
import retrofit2.Response

class AppointmentRepository(private val apiService: ApiService) {
    suspend fun getAppointments(): Response<List<AppointmentDto>> = apiService.getAppointments()
    suspend fun createAppointment(appointment: AppointmentDto): Response<AppointmentDto> = apiService.createAppointment(appointment)
    suspend fun updateAppointment(id: Long, appointment: AppointmentDto): Response<AppointmentDto> = apiService.updateAppointment(id, appointment)
    suspend fun deleteAppointment(id: Long): Response<Unit> = apiService.deleteAppointment(id)
}
