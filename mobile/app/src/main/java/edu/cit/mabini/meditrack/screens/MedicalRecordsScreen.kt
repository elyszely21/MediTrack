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
import edu.cit.mabini.meditrack.model.MedicalRecordDto
import edu.cit.mabini.meditrack.viewmodel.MedicalRecordViewModel
import edu.cit.mabini.meditrack.viewmodel.UIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen(navController: NavController, viewModel: MedicalRecordViewModel) {
    val recordsState by viewModel.recordsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchMedicalRecords()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medical Records", color = Color.White) },
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
                onClick = { /* TODO: Add record */ },
                containerColor = Color.Blue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = recordsState) {
                is UIState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UIState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = Color.Red)
                is UIState.Success -> {
                    if (state.data.isEmpty()) {
                        Text("No records found", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.data) { record ->
                                RecordItem(record)
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
fun RecordItem(record: MedicalRecordDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Visit Date: ${record.visitDate}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Diagnosis: ${record.diagnosis}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(text = "Treatment: ${record.treatment}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Prescription: ${record.prescription}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
