package edu.cit.mabini.meditrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.model.PatientDto
import edu.cit.mabini.meditrack.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PatientUiState {
    object Idle : PatientUiState()
    object Loading : PatientUiState()
    data class Success(val patients: List<PatientDto>) : PatientUiState()
    data class Error(val message: String) : PatientUiState()
}

sealed class PatientActionState {
    object Idle : PatientActionState()
    object Loading : PatientActionState()
    object Success : PatientActionState()
    data class Error(val message: String) : PatientActionState()
}

class PatientViewModel(private val repository: PatientRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientUiState>(PatientUiState.Idle)
    val uiState: StateFlow<PatientUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<PatientActionState>(PatientActionState.Idle)
    val actionState: StateFlow<PatientActionState> = _actionState.asStateFlow()

    fun loadPatients() {
        viewModelScope.launch {
            _uiState.value = PatientUiState.Loading
            try {
                val response = repository.getAll()
                if (response.isSuccessful) {
                    _uiState.value = PatientUiState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = PatientUiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = PatientUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun createPatient(dto: PatientDto) {
        viewModelScope.launch {
            _actionState.value = PatientActionState.Loading
            try {
                val response = repository.create(dto)
                if (response.isSuccessful) {
                    _actionState.value = PatientActionState.Success
                    loadPatients()
                } else {
                    _actionState.value = PatientActionState.Error("Failed to create patient")
                }
            } catch (e: Exception) {
                _actionState.value = PatientActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun updatePatient(id: Long, dto: PatientDto) {
        viewModelScope.launch {
            _actionState.value = PatientActionState.Loading
            try {
                val response = repository.update(id, dto)
                if (response.isSuccessful) {
                    _actionState.value = PatientActionState.Success
                    loadPatients()
                } else {
                    _actionState.value = PatientActionState.Error("Failed to update patient")
                }
            } catch (e: Exception) {
                _actionState.value = PatientActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun deletePatient(id: Long) {
        viewModelScope.launch {
            _actionState.value = PatientActionState.Loading
            try {
                val response = repository.delete(id)
                if (response.isSuccessful) {
                    _actionState.value = PatientActionState.Success
                    loadPatients()
                } else {
                    _actionState.value = PatientActionState.Error("Failed to delete patient")
                }
            } catch (e: Exception) {
                _actionState.value = PatientActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = PatientActionState.Idle
    }
}
