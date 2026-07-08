package edu.cit.mabini.meditrack.api

import edu.cit.mabini.meditrack.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    // Patients
    @GET("patients")
    suspend fun getPatients(): Response<List<PatientDto>>

    @POST("patients")
    suspend fun createPatient(@Body patient: PatientDto): Response<PatientDto>

    @PUT("patients/{id}")
    suspend fun updatePatient(@Path("id") id: Long, @Body patient: PatientDto): Response<PatientDto>

    @DELETE("patients/{id}")
    suspend fun deletePatient(@Path("id") id: Long): Response<Unit>

    // Appointments
    @GET("appointments")
    suspend fun getAppointments(): Response<List<AppointmentDto>>

    @POST("appointments")
    suspend fun createAppointment(@Body appointment: AppointmentDto): Response<AppointmentDto>

    @PUT("appointments/{id}")
    suspend fun updateAppointment(@Path("id") id: Long, @Body appointment: AppointmentDto): Response<AppointmentDto>

    @DELETE("appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: Long): Response<Unit>

    // Medical Records
    @GET("medical-records")
    suspend fun getMedicalRecords(): Response<List<MedicalRecordDto>>

    @POST("medical-records")
    suspend fun createMedicalRecord(@Body record: MedicalRecordDto): Response<MedicalRecordDto>

    @PUT("medical-records/{id}")
    suspend fun updateMedicalRecord(@Path("id") id: Long, @Body record: MedicalRecordDto): Response<MedicalRecordDto>

    @DELETE("medical-records/{id}")
    suspend fun deleteMedicalRecord(@Path("id") id: Long): Response<Unit>
}
