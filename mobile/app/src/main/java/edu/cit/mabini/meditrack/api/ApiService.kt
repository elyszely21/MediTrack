package edu.cit.mabini.meditrack.api

import edu.cit.mabini.meditrack.model.LoginRequest
import edu.cit.mabini.meditrack.model.LoginResponse
import edu.cit.mabini.meditrack.model.RegisterRequest
import edu.cit.mabini.meditrack.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<User>
}
