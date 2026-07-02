package edu.cit.mabini.meditrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.cit.mabini.meditrack.navigation.Screen
import edu.cit.mabini.meditrack.viewmodel.AuthState
import edu.cit.mabini.meditrack.viewmodel.AuthViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success<*>) {
            viewModel.resetState()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("MediTrack Login", fontSize = 28.sp, color = Color.Blue)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (error.isNotEmpty()) {
            Text(error, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authState is AuthState.Error) {
            Text((authState as AuthState.Error).message, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            ElevatedButton(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        error = "Please enter email and password"
                    } else {
                        error = ""
                        viewModel.login(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = Color.Blue, contentColor = Color.White)
            ) {
                Text("Login")
            }
        }

        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
            Text("Don't have an account? Register", color = Color.Gray)
        }
    }
}
