package edu.cit.mabini.meditrack.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import edu.cit.mabini.meditrack.api.RetrofitClient
import edu.cit.mabini.meditrack.repository.*
import edu.cit.mabini.meditrack.screens.*
import edu.cit.mabini.meditrack.session.SessionManager
import edu.cit.mabini.meditrack.viewmodel.*

@Composable
fun AppNavigation(context: Context) {
    val session = remember { SessionManager(context) }
    val api = remember { RetrofitClient.create(context) }

    val authViewModel = remember { AuthViewModel(AuthRepository(api), session) }
    val patientViewModel = remember { PatientViewModel(PatientRepository(api)) }
    val appointmentViewModel = remember { AppointmentViewModel(AppointmentRepository(api)) }
    val medicalRecordViewModel = remember { MedicalRecordViewModel(MedicalRecordRepository(api)) }

    val navController = rememberNavController()
    val startDestination = if (authViewModel.isLoggedIn()) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = authViewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToPatients = { navController.navigate("patients") },
                onNavigateToAppointments = { navController.navigate("appointments") },
                onNavigateToRecords = {
                    val role = authViewModel.getRole()
                    if (role == "SUPER_ADMIN" || role == "NURSE") {
                        // Staff go to Patients first or a generic list
                        navController.navigate("patients")
                    } else {
                        // Patients go directly to their own records
                        val myId = authViewModel.getUserId()
                        navController.navigate("records/$myId")
                    }
                },
                onNavigateToRegisterNurse = { navController.navigate("register_nurse") }
            )
        }

        composable("patients") {
            PatientsScreen(
                viewModel = patientViewModel,
                onBack = { navController.popBackStack() },
                onViewRecords = { patientId ->
                    navController.navigate("records/$patientId")
                }
            )
        }

        composable("appointments") {
            AppointmentsScreen(
                viewModel = appointmentViewModel,
                userRole = authViewModel.getRole(),
                userId = authViewModel.getUserId(),
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "records/{patientId}",
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: 0L
            MedicalRecordsScreen(
                patientId = patientId,
                viewModel = medicalRecordViewModel,
                userRole = authViewModel.getRole(),
                onBack = { navController.popBackStack() }
            )
        }

        composable("register_nurse") {
            RegisterNurseScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
    }
}
