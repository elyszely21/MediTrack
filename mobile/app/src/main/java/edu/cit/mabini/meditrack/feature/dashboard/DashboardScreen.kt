package edu.cit.mabini.meditrack.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Scaffold(
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Welcome back,", fontSize = 14.sp, color = Color(0xFF8B949E))
                    Text(viewModel.getFullName(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = if (isStaff) Color(0xFF1565C0) else Color(0xFF1B5E20),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = role.replace("_", " "),
                            color = if (isStaff) Color(0xFFBBDEFB) else Color(0xFFC8E6C9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Quick Access", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF8B949E))
            Spacer(modifier = Modifier.height(8.dp))

            // Only staff can see the Patients management card
            if (isStaff) {
                NavCard("Patients", "Manage clinic patient records", onNavigateToPatients)
                Spacer(modifier = Modifier.height(12.dp))
            }

            NavCard(
                title = if (isStaff) "Appointments" else "My Appointments",
                subtitle = if (isStaff) "Schedule and track clinic visits" else "View your upcoming clinic visits",
                onClick = onNavigateToAppointments
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            NavCard(
                title = if (isStaff) "Medical Records" else "My Records",
                subtitle = if (isStaff) "View all patient visit histories" else "View your personal health history",
                onClick = {
                    if (isStaff) {
                        onNavigateToRecords() // Goes to generic/search
                    } else {
                        onNavigateToRecords() // Will handle this in AppNavigation to use userId
                    }
                }
            )
            
            if (viewModel.isSuperAdmin()) {
                Spacer(modifier = Modifier.height(12.dp))
                NavCard("Register Nurse", "Add clinic nurse account", onNavigateToRegisterNurse)
            }

            Spacer(modifier = Modifier.weight(1f))

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
fun NavCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF8B949E))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8B949E))
        }
    }
}
