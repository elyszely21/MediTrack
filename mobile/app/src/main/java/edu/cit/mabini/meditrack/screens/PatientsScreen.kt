package edu.cit.mabini.meditrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.cit.mabini.meditrack.model.PatientDto
import edu.cit.mabini.meditrack.viewmodel.PatientViewModel
import edu.cit.mabini.meditrack.viewmodel.UIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(navController: NavController, viewModel: PatientViewModel) {
    val patientsState by viewModel.patientsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchPatients()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients", color = Color.White) },
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
                onClick = { /* TODO: Add patient dialog */ },
                containerColor = Color.Blue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Patient")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = patientsState) {
                is UIState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UIState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
                is UIState.Success -> {
                    if (state.data.isEmpty()) {
                        Text("No patients found", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.data) { patient ->
                                PatientItem(patient) {
                                    viewModel.deletePatient(patient.id!!)
                                }
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
fun PatientItem(patient: PatientDto, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "${patient.firstName} ${patient.lastName}", style = MaterialTheme.typography.titleLarge)
                Text(text = "ID: ${patient.patientNumber}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Contact: ${patient.contactNumber}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}
