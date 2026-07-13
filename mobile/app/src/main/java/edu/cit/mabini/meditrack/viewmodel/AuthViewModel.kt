package edu.cit.mabini.meditrack.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.feature.auth.LoginResponse
import edu.cit.mabini.meditrack.feature.auth.AuthRepository
import edu.cit.mabini.meditrack.core.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val data: LoginResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.login(email.trim(), password)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    session.save(body)
                    _uiState.value = AuthUiState.Success(body)
                } else {
                    _uiState.value = AuthUiState.Error(parseError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.register(
                    fullName.trim(),
                    email.trim(),
                    phoneNumber.trim(),
                    password,
                    confirmPassword
                )
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = AuthUiState.Success(response.body()!!)
                } else {
                    _uiState.value = AuthUiState.Error(parseError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun registerNurse(
        fullName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = repository.registerNurse(
                    fullName.trim(),
                    email.trim(),
                    phoneNumber.trim(),
                    password,
                    confirmPassword
                )
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = AuthUiState.Success(response.body()!!)
                } else {
                    _uiState.value = AuthUiState.Error(parseError(response.errorBody()?.string()))
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun logout() {
        session.clear()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun getUserId() = session.getUserId()
    fun getFullName() = session.getFullName() ?: "User"
    fun getRole() = session.getRole() ?: ""
    fun isSuperAdmin() = session.isSuperAdmin()
    fun isLoggedIn() = session.isLoggedIn()

    private fun parseError(raw: String?): String {
        if (raw == null) return "Something went wrong"
        return try {
            val key = "\"message\":\""
            if (raw.contains(key)) {
                val start = raw.indexOf(key) + key.length
                val end = raw.indexOf("\"", start)
                val msg = raw.substring(start, end)
                if (msg.isNotBlank()) msg else raw
            } else {
                raw
            }
        } catch (e: Exception) {
            raw
        }
    }
}
