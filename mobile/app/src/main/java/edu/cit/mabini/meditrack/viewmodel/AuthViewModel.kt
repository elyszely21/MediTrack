package edu.cit.mabini.meditrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.model.LoginRequest
import edu.cit.mabini.meditrack.model.LoginResponse
import edu.cit.mabini.meditrack.model.RegisterRequest
import edu.cit.mabini.meditrack.repository.AuthRepository
import edu.cit.mabini.meditrack.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success<T>(val data: T) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    sessionManager.saveSession(
                        token = loginResponse.token,
                        fullName = loginResponse.fullName,
                        email = loginResponse.email,
                        role = loginResponse.role
                    )
                    _authState.value = AuthState.Success(loginResponse)
                } else {
                    _authState.value = AuthState.Error(response.errorBody()?.string() ?: "Login failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun register(fullName: String, email: String, phoneNumber: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.register(RegisterRequest(fullName, email, phoneNumber, password, confirmPassword))
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    sessionManager.saveSession(
                        token = loginResponse.token,
                        fullName = loginResponse.fullName,
                        email = loginResponse.email,
                        role = loginResponse.role
                    )
                    _authState.value = AuthState.Success(loginResponse)
                } else {
                    _authState.value = AuthState.Error(response.errorBody()?.string() ?: "Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _authState.value = AuthState.Idle
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
    
    fun getFullName(): String = sessionManager.getFullName() ?: ""
    fun getRole(): String = sessionManager.getRole() ?: ""

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
