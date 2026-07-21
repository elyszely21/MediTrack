package edu.cit.mabini.meditrack.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.mabini.meditrack.feature.auth.AuthViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    viewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateToPatients: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    onNavigateToRecords: () -> Unit,
    onNavigateToRegisterNurse: () -> Unit
) {
    val role = viewModel.getRole()
    val isStaff = role == "SUPER_ADMIN" || role == "NURSE" || role == "DOCTOR"
    val isPatient = role == "PATIENT"

    Scaffold(
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header Section mirroring React's welcome
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = when (role) {
                        "SUPER_ADMIN" -> "Admin Overview"
                        "DOCTOR" -> "Doctor Dashboard"
                        "NURSE" -> "Nurse Dashboard"
                        else -> "Patient portal"
                    }.uppercase(),
                    fontSize = 12.sp,
                    color = Color(0xFF8B949E),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (role == "DOCTOR") "Welcome, Dr. ${viewModel.getFullName()}" else "Welcome, ${viewModel.getFullName()}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")),
                    fontSize = 14.sp,
                    color = Color(0xFF8B949E),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Hero/Badge Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isStaff) "Clinic Access" else "Patient Access",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = role.replace("_", " "),
                            fontSize = 13.sp,
                            color = Color(0xFF8B949E)
                        )
                    }
                    Surface(
                        color = if (isStaff) Color(0xFF1565C0) else Color(0xFF1B5E20),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Quick Access", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Grid/List based on role
            if (isStaff) {
                NavCard("Patients", "View and manage patient records", onNavigateToPatients, "👥", Color(0xFF2196F3))
                Spacer(modifier = Modifier.height(12.dp))
            }

            NavCard(
                title = if (isStaff) "Appointments" else "My Appointments",
                subtitle = if (isStaff) "Schedule and track clinic visits" else "View your upcoming clinic visits",
                onClick = onNavigateToAppointments,
                icon = "📅",
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            NavCard(
                title = if (isStaff) "Medical Records" else "My Records",
                subtitle = if (isStaff) "Access patient visit histories" else "View your personal health history",
                onClick = onNavigateToRecords,
                icon = "📋",
                color = Color(0xFF9C27B0)
            )
            
            if (viewModel.isSuperAdmin()) {
                Spacer(modifier = Modifier.height(12.dp))
                NavCard("Register Nurse", "Create new nurse account", onNavigateToRegisterNurse, "👩‍⚕️", Color(0xFF673AB7))
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF5350))),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350))
            ) {
                Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NavCard(title: String, subtitle: String, onClick: () -> Unit, icon: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF8B949E))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8B949E))
        }
    }
}
