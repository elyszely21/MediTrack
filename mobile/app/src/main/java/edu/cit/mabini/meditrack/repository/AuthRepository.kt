package edu.cit.mabini.meditrack.repository

import edu.cit.mabini.meditrack.api.ApiService
import edu.cit.mabini.meditrack.model.LoginRequest
import edu.cit.mabini.meditrack.model.LoginResponse
import edu.cit.mabini.meditrack.model.RegisterRequest
import retrofit2.Response

class AuthRepository(private val api: ApiService) {
    suspend fun login(email: String, password: String): Response<LoginResponse> =
        api.login(LoginRequest(email, password))

    suspend fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ): Response<LoginResponse> =
        api.register(RegisterRequest(fullName, email, phoneNumber, password, confirmPassword))

    suspend fun registerNurse(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ): Response<LoginResponse> =
        api.registerNurse(RegisterRequest(fullName, email, phoneNumber, password, confirmPassword))
}
