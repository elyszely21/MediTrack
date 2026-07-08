package edu.cit.mabini.meditrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.cit.mabini.meditrack.navigation.Screen
import edu.cit.mabini.meditrack.ui.theme.*
import edu.cit.mabini.meditrack.viewmodel.AuthState
import edu.cit.mabini.meditrack.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success<*>) {
            viewModel.resetState()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFEAF4FF), Color(0xFFF8FBFF))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
                Spacer(modifier = Modifier.height(24.dp))

                RegisterField(label = "Full Name", value = fullName, onValueChange = { fullName = it }, placeholder = "John Doe")
                RegisterField(label = "Email", value = email, onValueChange = { email = it }, placeholder = "you@example.com")
                RegisterField(label = "Phone Number", value = phoneNumber, onValueChange = { phoneNumber = it }, placeholder = "09123456789")
                RegisterField(label = "Password", value = password, onValueChange = { password = it }, placeholder = "Enter password", isPassword = true)
                RegisterField(label = "Confirm Password", value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = "Confirm password", isPassword = true)

                Spacer(modifier = Modifier.height(24.dp))

                if (error.isNotEmpty()) {
                    Text(error, color = ErrorRed, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (authState is AuthState.Error) {
                    Text((authState as AuthState.Error).message, color = ErrorRed, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = {
                            if (fullName.isBlank() || email.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
                                error = "Please fill in all fields"
                            } else if (password != confirmPassword) {
                                error = "Passwords do not match"
                            } else {
                                error = ""
                                viewModel.register(fullName, email, phoneNumber, password, confirmPassword)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Register", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Already have an account? ", color = TextGray)
                    Text(
                        text = "Login",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Login.route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, isPassword: Boolean = false) {
    Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = BorderBlue
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
}
