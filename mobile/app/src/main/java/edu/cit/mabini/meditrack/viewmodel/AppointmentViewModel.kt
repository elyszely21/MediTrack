package edu.cit.mabini.meditrack.feature.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.feature.appointments.data.model.AppointmentDto
import edu.cit.mabini.meditrack.feature.appointments.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AppointmentUiState {
    object Idle : AppointmentUiState()
    object Loading : AppointmentUiState()
    data class Success(val appointments: List<AppointmentDto>) : AppointmentUiState()
    data class Error(val message: String) : AppointmentUiState()
}

sealed class AppointmentActionState {
    object Idle : AppointmentActionState()
    object Loading : AppointmentActionState()
    object Success : AppointmentActionState()
    data class Error(val message: String) : AppointmentActionState()
}

class AppointmentViewModel(private val repository: AppointmentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AppointmentUiState>(AppointmentUiState.Idle)
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<AppointmentActionState>(AppointmentActionState.Idle)
    val actionState: StateFlow<AppointmentActionState> = _actionState.asStateFlow()

    fun loadAppointments() {
        viewModelScope.launch {
            _uiState.value = AppointmentUiState.Loading
            try {
                val response = repository.getAll()
                if (response.isSuccessful) {
                    _uiState.value = AppointmentUiState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = AppointmentUiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = AppointmentUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun createAppointment(dto: AppointmentDto) {
        viewModelScope.launch {
            _actionState.value = AppointmentActionState.Loading
            try {
                val response = repository.create(dto)
                if (response.isSuccessful) {
                    _actionState.value = AppointmentActionState.Success
                    loadAppointments()
                } else {
                    _actionState.value = AppointmentActionState.Error("Failed to create appointment")
                }
            } catch (e: Exception) {
                _actionState.value = AppointmentActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun updateAppointment(id: Long, dto: AppointmentDto) {
        viewModelScope.launch {
            _actionState.value = AppointmentActionState.Loading
            try {
                val response = repository.update(id, dto)
                if (response.isSuccessful) {
                    _actionState.value = AppointmentActionState.Success
                    loadAppointments()
                } else {
                    _actionState.value = AppointmentActionState.Error("Failed to update appointment")
                }
            } catch (e: Exception) {
                _actionState.value = AppointmentActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun deleteAppointment(id: Long) {
        viewModelScope.launch {
            _actionState.value = AppointmentActionState.Loading
            try {
                val response = repository.delete(id)
                if (response.isSuccessful) {
                    _actionState.value = AppointmentActionState.Success
                    loadAppointments()
                } else {
                    _actionState.value = AppointmentActionState.Error("Failed to delete appointment")
                }
            } catch (e: Exception) {
                _actionState.value = AppointmentActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = AppointmentActionState.Idle
    }
}
