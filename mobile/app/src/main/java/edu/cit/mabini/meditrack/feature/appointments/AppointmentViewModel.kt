package edu.cit.mabini.meditrack.feature.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.mabini.meditrack.feature.appointments.data.model.AppointmentDto
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientLookupDto
import edu.cit.mabini.meditrack.feature.doctors.data.model.DoctorDto
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

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _doctors = MutableStateFlow<List<DoctorDto>>(emptyList())
    val doctors: StateFlow<List<DoctorDto>> = _doctors.asStateFlow()

    fun setStatusFilter(status: String, isStaff: Boolean = true) {
        _statusFilter.value = status
        loadAppointments(isStaff)
    }

    fun loadAppointments(isStaff: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = AppointmentUiState.Loading
            try {
                val response = if (!isStaff) {
                    repository.getMyAppointments()
                } else if (_statusFilter.value == "ALL") {
                    repository.getAll()
                } else {
                    repository.getByStatus(_statusFilter.value)
                }
                
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

    fun loadDoctors() {
        viewModelScope.launch {
            try {
                val response = repository.getDoctors()
                if (response.isSuccessful) {
                    _doctors.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) { /* Silent fail */ }
        }
    }

    fun lookupPatient(number: String, onResult: (PatientLookupDto?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.lookupPatientByNumber(number)
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun performAction(id: Long, action: String, isStaff: Boolean = true, reason: String? = null) {
        viewModelScope.launch {
            _actionState.value = AppointmentActionState.Loading
            try {
                val response = when (action) {
                    "approve" -> repository.approve(id)
                    "reject" -> repository.reject(id, reason)
                    "check-in" -> repository.checkIn(id)
                    "waiting" -> repository.waiting(id)
                    "in-consultation" -> repository.startConsultation(id)
                    "prescription-issued" -> repository.issuePrescription(id)
                    "complete" -> repository.complete(id)
                    "cancel" -> repository.cancel(id, reason)
                    "no-show" -> repository.noShow(id, reason)
                    "delete" -> {
                        val delRes = repository.delete(id)
                        if (delRes.isSuccessful) {
                            _actionState.value = AppointmentActionState.Success
                            loadAppointments(isStaff)
                        } else {
                            _actionState.value = AppointmentActionState.Error("Failed to delete")
                        }
                        return@launch
                    }
                    else -> throw IllegalArgumentException("Unknown action")
                }

                if (response.isSuccessful) {
                    _actionState.value = AppointmentActionState.Success
                    loadAppointments(isStaff)
                } else {
                    _actionState.value = AppointmentActionState.Error("Action failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _actionState.value = AppointmentActionState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun createAppointment(dto: AppointmentDto, isStaff: Boolean = true) {
        viewModelScope.launch {
            _actionState.value = AppointmentActionState.Loading
            try {
                val response = repository.create(dto)
                if (response.isSuccessful) {
                    _actionState.value = AppointmentActionState.Success
                    loadAppointments(isStaff)
                } else {
                    _actionState.value = AppointmentActionState.Error("Failed to create appointment")
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
