package edu.cit.mabini.meditrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.model.PatientDto
import edu.cit.mabini.meditrack.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UIState<out T> {
    object Idle : UIState<Nothing>()
    object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val message: String) : UIState<Nothing>()
}

class PatientViewModel(private val repository: PatientRepository) : ViewModel() {

    private val _patientsState = MutableStateFlow<UIState<List<PatientDto>>>(UIState.Idle)
    val patientsState: StateFlow<UIState<List<PatientDto>>> = _patientsState.asStateFlow()

    fun fetchPatients() {
        viewModelScope.launch {
            _patientsState.value = UIState.Loading
            try {
                val response = repository.getPatients()
                if (response.isSuccessful) {
                    _patientsState.value = UIState.Success(response.body() ?: emptyList())
                } else {
                    _patientsState.value = UIState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _patientsState.value = UIState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun addPatient(patient: PatientDto, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.createPatient(patient)
                if (response.isSuccessful) {
                    fetchPatients()
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deletePatient(id: Long) {
        viewModelScope.launch {
            try {
                val response = repository.deletePatient(id)
                if (response.isSuccessful) {
                    fetchPatients()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
