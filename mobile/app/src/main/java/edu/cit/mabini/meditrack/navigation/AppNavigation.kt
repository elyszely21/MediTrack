package edu.cit.mabini.meditrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cit.mabini.meditrack.screens.DashboardScreen
import edu.cit.mabini.meditrack.screens.LoginScreen
import edu.cit.mabini.meditrack.screens.RegisterScreen
import edu.cit.mabini.meditrack.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }
    }
}
