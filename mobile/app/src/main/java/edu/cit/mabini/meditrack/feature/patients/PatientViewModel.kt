package edu.cit.mabini.meditrack.feature.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientDto
import edu.cit.mabini.meditrack.feature.patients.PatientRepository
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

    fun loadPatients(role: String, searchQuery: String = "", showArchived: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = PatientUiState.Loading
            try {
                val isAdmin = role == "SUPER_ADMIN"
                val response = if (searchQuery.isNotBlank()) {
                    if (isAdmin) {
                        repository.search(searchQuery.trim(), showArchived)
                    } else {
                        repository.staffLookup(searchQuery.trim())
                    }
                } else {
                    if (isAdmin) {
                        if (showArchived) repository.getArchived() else repository.getAll()
                    } else {
                        repository.staffLookup()
                    }
                }

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

    fun createPatient(role: String, dto: PatientDto) {
        viewModelScope.launch {
            _actionState.value = PatientActionState.Loading
            try {
                val response = repository.create(dto)
                if (response.isSuccessful) {
                    _actionState.value = PatientActionState.Success
                    loadPatients(role)
                } else {
                    _actionState.value = PatientActionState.Error("Failed to create patient")
                }
            } catch (e: Exception) {
                _actionState.value = PatientActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun updatePatient(role: String, id: Long, dto: PatientDto) {
        viewModelScope.launch {
            _actionState.value = PatientActionState.Loading
            try {
                val response = repository.update(id, dto)
                if (response.isSuccessful) {
                    _actionState.value = PatientActionState.Success
                    loadPatients(role)
                } else {
                    _actionState.value = PatientActionState.Error("Failed to update patient")
                }
            } catch (e: Exception) {
                _actionState.value = PatientActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun archivePatient(role: String, id: Long, isArchived: Boolean) {
        viewModelScope.launch {
            _actionState.value = PatientActionState.Loading
            try {
                val response = if (isArchived) repository.unarchive(id) else repository.archive(id)
                if (response.isSuccessful) {
                    _actionState.value = PatientActionState.Success
                    loadPatients(role)
                } else {
                    _actionState.value = PatientActionState.Error("Failed to update patient status")
                }
            } catch (e: Exception) {
                _actionState.value = PatientActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun deletePatient(role: String, id: Long) {
        viewModelScope.launch {
            _actionState.value = PatientActionState.Loading
            try {
                val response = repository.delete(id)
                if (response.isSuccessful) {
                    _actionState.value = PatientActionState.Success
                    loadPatients(role)
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
