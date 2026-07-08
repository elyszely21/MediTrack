package edu.cit.mabini.meditrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.model.MedicalRecordDto
import edu.cit.mabini.meditrack.repository.MedicalRecordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedicalRecordViewModel(private val repository: MedicalRecordRepository) : ViewModel() {

    private val _recordsState = MutableStateFlow<UIState<List<MedicalRecordDto>>>(UIState.Idle)
    val recordsState: StateFlow<UIState<List<MedicalRecordDto>>> = _recordsState.asStateFlow()

    fun fetchMedicalRecords() {
        viewModelScope.launch {
            _recordsState.value = UIState.Loading
            try {
                val response = repository.getMedicalRecords()
                if (response.isSuccessful) {
                    _recordsState.value = UIState.Success(response.body() ?: emptyList())
                } else {
                    _recordsState.value = UIState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _recordsState.value = UIState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}
