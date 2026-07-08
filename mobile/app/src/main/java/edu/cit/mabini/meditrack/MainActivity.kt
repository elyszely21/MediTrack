package edu.cit.mabini.meditrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import edu.cit.mabini.meditrack.api.RetrofitClient
import edu.cit.mabini.meditrack.navigation.AppNavigation
import edu.cit.mabini.meditrack.repository.*
import edu.cit.mabini.meditrack.ui.theme.MeditrackTheme
import edu.cit.mabini.meditrack.util.SessionManager
import edu.cit.mabini.meditrack.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SessionManager
        val sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)

        // Initialize API Service
        val apiService = RetrofitClient.apiService

        // Initialize Repositories
        val authRepository = AuthRepository(apiService)
        val patientRepository = PatientRepository(apiService)
        val appointmentRepository = AppointmentRepository(apiService)
        val medicalRecordRepository = MedicalRecordRepository(apiService)

        // Initialize ViewModels
        val authViewModel = AuthViewModel(authRepository, sessionManager)
        val patientViewModel = PatientViewModel(patientRepository)
        val appointmentViewModel = AppointmentViewModel(appointmentRepository)
        val medicalRecordViewModel = MedicalRecordViewModel(medicalRecordRepository)

        setContent {
            MeditrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        patientViewModel = patientViewModel,
                        appointmentViewModel = appointmentViewModel,
                        medicalRecordViewModel = medicalRecordViewModel
                    )
                }
            }
        }
    }
}
