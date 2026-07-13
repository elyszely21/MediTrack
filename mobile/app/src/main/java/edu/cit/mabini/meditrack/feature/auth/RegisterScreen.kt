package edu.cit.mabini.meditrack.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.mabini.meditrack.feature.auth.AuthUiState
import edu.cit.mabini.meditrack.feature.auth.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MediTrack",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2196F3)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    RegisterInputField(
                        label = "Full Name",
                        value = fullName,
                        onValueChange = { fullName = it },
                        keyboardType = KeyboardType.Text
                    )
                    RegisterInputField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        keyboardType = KeyboardType.Email
                    )
                    RegisterInputField(
                        label = "Phone Number",
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        keyboardType = KeyboardType.Phone
                    )
                    RegisterInputField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        keyboardType = KeyboardType.Password,
                        isPassword = true
                    )
                    RegisterInputField(
                        label = "Confirm Password",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        keyboardType = KeyboardType.Password,
                        isPassword = true
                    )

                    if (localError.isNotEmpty()) {
                        Text(localError, color = Color(0xFFEF5350), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (uiState is AuthUiState.Error) {
                        Text((uiState as AuthUiState.Error).message, color = Color(0xFFEF5350), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (fullName.isBlank()) localError = "Full name is required"
                            else if (email.isBlank()) localError = "Email is required"
                            else if (phoneNumber.isBlank()) localError = "Phone number is required"
                            else if (phoneNumber.length < 10) localError = "Enter a valid phone number"
                            else if (password.isBlank()) localError = "Password is required"
                            else if (password != confirmPassword) localError = "Passwords do not match"
                            else {
                                localError = ""
                                viewModel.register(fullName, email, phoneNumber, password, confirmPassword)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        enabled = uiState !is AuthUiState.Loading
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Register", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = Color(0xFF8B949E))
                Text(
                    text = "Sign In",
                    color = Color(0xFF2196F3),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Composable
fun RegisterInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    isPassword: Boolean = false
) {
    Text(label, fontSize = 12.sp, color = Color(0xFF8B949E))
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2196F3),
            unfocusedBorderColor = Color(0xFF30363D),
            focusedLabelColor = Color(0xFF2196F3),
            unfocusedLabelColor = Color(0xFF8B949E),
            cursorColor = Color(0xFF2196F3),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0xFF21262D),
            unfocusedContainerColor = Color(0xFF21262D)
        ),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(12.dp))
}
