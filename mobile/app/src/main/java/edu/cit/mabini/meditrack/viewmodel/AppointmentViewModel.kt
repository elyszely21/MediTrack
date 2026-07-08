package edu.cit.mabini.meditrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.model.AppointmentDto
import edu.cit.mabini.meditrack.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppointmentViewModel(private val repository: AppointmentRepository) : ViewModel() {

    private val _appointmentsState = MutableStateFlow<UIState<List<AppointmentDto>>>(UIState.Idle)
    val appointmentsState: StateFlow<UIState<List<AppointmentDto>>> = _appointmentsState.asStateFlow()

    fun fetchAppointments() {
        viewModelScope.launch {
            _appointmentsState.value = UIState.Loading
            try {
                val response = repository.getAppointments()
                if (response.isSuccessful) {
                    _appointmentsState.value = UIState.Success(response.body() ?: emptyList())
                } else {
                    _appointmentsState.value = UIState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _appointmentsState.value = UIState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun createAppointment(appointment: AppointmentDto, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.createAppointment(appointment)
                if (response.isSuccessful) {
                    fetchAppointments()
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
