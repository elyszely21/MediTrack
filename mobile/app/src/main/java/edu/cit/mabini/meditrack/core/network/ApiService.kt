package edu.cit.mabini.meditrack.core.network

import edu.cit.mabini.meditrack.feature.medicalrecords.data.model.MedicalRecordDto
import edu.cit.mabini.meditrack.feature.appointments.data.model.AppointmentDto
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientDto
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientLookupDto
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientProfileDto
import edu.cit.mabini.meditrack.feature.doctors.data.model.DoctorDto
import edu.cit.mabini.meditrack.feature.auth.RegisterRequest
import edu.cit.mabini.meditrack.feature.auth.LoginResponse
import edu.cit.mabini.meditrack.feature.auth.LoginRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/admin/register-nurse")
    suspend fun registerNurse(@Body request: RegisterRequest): Response<LoginResponse>

    // Patients
    @GET("patients")
    suspend fun getPatients(): Response<List<PatientDto>>

    @GET("patients/archived")
    suspend fun getArchivedPatients(): Response<List<PatientDto>>

    @GET("patients/staff-lookup")
    suspend fun getStaffLookupPatients(@Query("q") query: String? = null): Response<List<PatientDto>>

    @GET("patients/search")
    suspend fun searchPatients(
        @Query("q") query: String,
        @Query("archived") archived: Boolean = false
    ): Response<List<PatientDto>>

    @GET("patients/{id}")
    suspend fun getPatient(@Path("id") id: Long): Response<PatientDto>

    @POST("patients")
    suspend fun createPatient(@Body dto: PatientDto): Response<PatientDto>

    @PUT("patients/{id}")
    suspend fun updatePatient(@Path("id") id: Long, @Body dto: PatientDto): Response<PatientDto>

    @PUT("patients/{id}/archive")
    suspend fun archivePatient(@Path("id") id: Long): Response<Unit>

    @PUT("patients/{id}/unarchive")
    suspend fun unarchivePatient(@Path("id") id: Long): Response<Unit>

    @DELETE("patients/{id}")
    suspend fun deletePatient(@Path("id") id: Long): Response<Unit>

    // Medical Records
    @GET("records/patient/{patientId}")
    suspend fun getRecordsByPatient(@Path("patientId") patientId: Long): Response<List<MedicalRecordDto>>

    @POST("records")
    suspend fun createRecord(@Body dto: MedicalRecordDto): Response<MedicalRecordDto>

    // Appointments
    @GET("appointments")
    suspend fun getAppointments(): Response<List<AppointmentDto>>

    @GET("appointments/date/{date}")
    suspend fun getAppointmentsByDate(@Path("date") date: String): Response<List<AppointmentDto>>

    @GET("appointments/patient/{patientId}")
    suspend fun getAppointmentsByPatient(@Path("patientId") patientId: Long): Response<List<AppointmentDto>>

    @POST("appointments")
    suspend fun createAppointment(@Body dto: AppointmentDto): Response<AppointmentDto>

    @PUT("appointments/{id}")
    suspend fun updateAppointment(@Path("id") id: Long, @Body dto: AppointmentDto): Response<AppointmentDto>

    @PUT("appointments/{id}/approve")
    suspend fun approveAppointment(@Path("id") id: Long): Response<AppointmentDto>

    @PUT("appointments/{id}/reject")
    suspend fun rejectAppointment(@Path("id") id: Long, @Body reason: Map<String, String?>): Response<AppointmentDto>

    @PUT("appointments/{id}/check-in")
    suspend fun checkInAppointment(@Path("id") id: Long): Response<AppointmentDto>

    @PUT("appointments/{id}/waiting")
    suspend fun waitingAppointment(@Path("id") id: Long): Response<AppointmentDto>

    @PUT("appointments/{id}/in-consultation")
    suspend fun startConsultationAppointment(@Path("id") id: Long): Response<AppointmentDto>

    @PUT("appointments/{id}/prescription-issued")
    suspend fun issuePrescriptionAppointment(@Path("id") id: Long): Response<AppointmentDto>

    @PUT("appointments/{id}/complete")
    suspend fun completeAppointment(@Path("id") id: Long): Response<AppointmentDto>

    @PUT("appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") id: Long, @Body reason: Map<String, String?>): Response<AppointmentDto>

    @PUT("appointments/{id}/no-show")
    suspend fun noShowAppointment(@Path("id") id: Long, @Body reason: Map<String, String?>): Response<AppointmentDto>

    @GET("appointments/lookup-patient/{patientNumber}")
    suspend fun lookupPatientByNumber(@Path("patientNumber") patientNumber: String): Response<PatientLookupDto>

    @GET("appointments/status/{status}")
    suspend fun getAppointmentsByStatus(@Path("status") status: String): Response<List<AppointmentDto>>

    @GET("appointments/doctor/dashboard")
    suspend fun getDoctorDashboardStats(): Response<Map<String, Any>>

    @DELETE("appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: Long): Response<Unit>

    // Users & Doctors
    @GET("users")
    suspend fun getUsers(@Query("role") role: String? = null): Response<List<Map<String, Any>>>

    @GET("doctors")
    suspend fun getDoctors(): Response<List<DoctorDto>>

    // Patient Portal
    @GET("patient-portal/me")
    suspend fun getMyProfile(): Response<PatientProfileDto>

    @GET("patient-portal/appointments")
    suspend fun getMyAppointments(): Response<List<AppointmentDto>>

    @GET("patient-portal/medical-records")
    suspend fun getMyRecords(): Response<List<MedicalRecordDto>>
}
