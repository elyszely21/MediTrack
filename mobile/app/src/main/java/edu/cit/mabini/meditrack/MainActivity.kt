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
import edu.cit.mabini.meditrack.repository.AuthRepository
import edu.cit.mabini.meditrack.ui.theme.MeditrackTheme
import edu.cit.mabini.meditrack.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Repository and ViewModel
        // In a real production app, consider using Dagger Hilt for Dependency Injection
        val apiService = RetrofitClient.apiService
        val authRepository = AuthRepository(apiService)
        val authViewModel = AuthViewModel(authRepository)

        setContent {
            MeditrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(authViewModel = authViewModel)
                }
            }
        }
    }
}
