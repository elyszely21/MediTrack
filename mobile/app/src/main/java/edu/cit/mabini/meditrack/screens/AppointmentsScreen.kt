package edu.cit.mabini.meditrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.cit.mabini.meditrack.model.AppointmentDto
import edu.cit.mabini.meditrack.viewmodel.AppointmentViewModel
import edu.cit.mabini.meditrack.viewmodel.UIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(navController: NavController, viewModel: AppointmentViewModel) {
    val appointmentsState by viewModel.appointmentsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAppointments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Blue),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Create appointment */ },
                containerColor = Color.Blue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Appointment")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = appointmentsState) {
                is UIState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UIState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
                is UIState.Success -> {
                    if (state.data.isEmpty()) {
                        Text("No appointments found", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.data) { appointment ->
                                AppointmentItem(appointment)
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun AppointmentItem(appointment: AppointmentDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Date: ${appointment.appointmentDate} at ${appointment.appointmentTime}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Status: ${appointment.status}", color = if (appointment.status == "CANCELLED") Color.Red else Color.DarkGray)
            Text(text = "Remarks: ${appointment.remarks}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
