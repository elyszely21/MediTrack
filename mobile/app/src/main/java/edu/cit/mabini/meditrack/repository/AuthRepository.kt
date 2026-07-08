package edu.cit.mabini.meditrack.repository

import edu.cit.mabini.meditrack.api.ApiService
import edu.cit.mabini.meditrack.model.LoginRequest
import edu.cit.mabini.meditrack.model.LoginResponse
import edu.cit.mabini.meditrack.model.RegisterRequest
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {
    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse> {
        return apiService.login(loginRequest)
    }

    suspend fun register(registerRequest: RegisterRequest): Response<LoginResponse> {
        return apiService.register(registerRequest)
    }
}
