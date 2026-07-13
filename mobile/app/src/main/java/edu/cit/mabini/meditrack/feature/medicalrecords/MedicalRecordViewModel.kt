package edu.cit.mabini.meditrack.feature.medicalrecords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.feature.medicalrecords.data.model.MedicalRecordDto
import edu.cit.mabini.meditrack.feature.medicalrecords.MedicalRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RecordUiState {
    object Idle : RecordUiState()
    object Loading : RecordUiState()
    data class Success(val records: List<MedicalRecordDto>) : RecordUiState()
    data class Error(val message: String) : RecordUiState()
}

sealed class RecordActionState {
    object Idle : RecordActionState()
    object Loading : RecordActionState()
    object Success : RecordActionState()
    data class Error(val message: String) : RecordActionState()
}

class MedicalRecordViewModel(private val repository: MedicalRecordRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordUiState>(RecordUiState.Idle)
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<RecordActionState>(RecordActionState.Idle)
    val actionState: StateFlow<RecordActionState> = _actionState.asStateFlow()

    fun loadRecords(patientId: Long) {
        viewModelScope.launch {
            _uiState.value = RecordUiState.Loading
            try {
                val response = repository.getByPatient(patientId)
                if (response.isSuccessful) {
                    _uiState.value = RecordUiState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = RecordUiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = RecordUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun createRecord(dto: MedicalRecordDto) {
        viewModelScope.launch {
            _actionState.value = RecordActionState.Loading
            try {
                val response = repository.create(dto)
                if (response.isSuccessful) {
                    _actionState.value = RecordActionState.Success
                    loadRecords(dto.patientId)
                } else {
                    _actionState.value = RecordActionState.Error("Failed to create record")
                }
            } catch (e: Exception) {
                _actionState.value = RecordActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = RecordActionState.Idle
    }
}
