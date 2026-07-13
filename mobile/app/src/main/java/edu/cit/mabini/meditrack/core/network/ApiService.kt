package edu.cit.mabini.meditrack.core.network

import edu.cit.mabini.meditrack.feature.medicalrecords.data.model.MedicalRecordDto
import edu.cit.mabini.meditrack.feature.appointments.data.model.AppointmentDto
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientDto
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

    @GET("patients/{id}")
    suspend fun getPatient(@Path("id") id: Long): Response<PatientDto>

    @POST("patients")
    suspend fun createPatient(@Body dto: PatientDto): Response<PatientDto>

    @PUT("patients/{id}")
    suspend fun updatePatient(@Path("id") id: Long, @Body dto: PatientDto): Response<PatientDto>

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

    @DELETE("appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: Long): Response<Unit>
}
