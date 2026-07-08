package edu.cit.mabini.meditrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cit.mabini.meditrack.screens.*
import edu.cit.mabini.meditrack.viewmodel.*

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Patients : Screen("patients")
    object Appointments : Screen("appointments")
    object MedicalRecords : Screen("medical_records")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    patientViewModel: PatientViewModel,
    appointmentViewModel: AppointmentViewModel,
    medicalRecordViewModel: MedicalRecordViewModel
) {
    val navController = rememberNavController()
    val startDestination = if (authViewModel.isLoggedIn()) Screen.Dashboard.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, viewModel = authViewModel)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController, viewModel = authViewModel)
        }
        composable(Screen.Patients.route) {
            PatientsScreen(navController = navController, viewModel = patientViewModel)
        }
        composable(Screen.Appointments.route) {
            AppointmentsScreen(navController = navController, viewModel = appointmentViewModel)
        }
        composable(Screen.MedicalRecords.route) {
            MedicalRecordsScreen(navController = navController, viewModel = medicalRecordViewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, viewModel = authViewModel)
        }
    }
}
