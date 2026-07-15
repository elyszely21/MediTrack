package edu.cit.mabini.meditrack.feature.appointments

import edu.cit.mabini.meditrack.core.network.ApiService
import edu.cit.mabini.meditrack.feature.appointments.data.model.AppointmentDto
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientLookupDto
import edu.cit.mabini.meditrack.feature.doctors.data.model.DoctorDto
import retrofit2.Response

class AppointmentRepository(private val api: ApiService) {
    suspend fun getAll(): Response<List<AppointmentDto>> = api.getAppointments()

    suspend fun getByDate(date: String): Response<List<AppointmentDto>> =
        api.getAppointmentsByDate(date)

    suspend fun getByPatient(id: Long): Response<List<AppointmentDto>> =
        api.getAppointmentsByPatient(id)

    suspend fun getByStatus(status: String): Response<List<AppointmentDto>> =
        api.getAppointmentsByStatus(status)

    suspend fun getMyAppointments(): Response<List<AppointmentDto>> =
        api.getMyAppointments()

    suspend fun create(dto: AppointmentDto): Response<AppointmentDto> =
        api.createAppointment(dto)

    suspend fun update(id: Long, dto: AppointmentDto): Response<AppointmentDto> =
        api.updateAppointment(id, dto)

    suspend fun approve(id: Long): Response<AppointmentDto> =
        api.approveAppointment(id)

    suspend fun reject(id: Long, reason: String?): Response<AppointmentDto> =
        api.rejectAppointment(id, mapOf("reason" to reason))

    suspend fun checkIn(id: Long): Response<AppointmentDto> =
        api.checkInAppointment(id)

    suspend fun waiting(id: Long): Response<AppointmentDto> =
        api.waitingAppointment(id)

    suspend fun startConsultation(id: Long): Response<AppointmentDto> =
        api.startConsultationAppointment(id)

    suspend fun issuePrescription(id: Long): Response<AppointmentDto> =
        api.issuePrescriptionAppointment(id)

    suspend fun complete(id: Long): Response<AppointmentDto> =
        api.completeAppointment(id)

    suspend fun cancel(id: Long, reason: String?): Response<AppointmentDto> =
        api.cancelAppointment(id, mapOf("reason" to reason))

    suspend fun noShow(id: Long, reason: String?): Response<AppointmentDto> =
        api.noShowAppointment(id, mapOf("reason" to reason))

    suspend fun delete(id: Long): Response<Unit> = api.deleteAppointment(id)

    suspend fun lookupPatientByNumber(patientNumber: String): Response<PatientLookupDto> =
        api.lookupPatientByNumber(patientNumber)

    suspend fun getDoctors(): Response<List<DoctorDto>> =
        api.getDoctors()

    suspend fun getUsers(role: String? = null): Response<List<Map<String, Any>>> =
        api.getUsers(role)
}
