package edu.cit.mabini.meditrack.feature.medicalrecords

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.mabini.meditrack.feature.medicalrecords.data.model.MedicalRecordDto
import edu.cit.mabini.meditrack.viewmodel.RecordActionState
import edu.cit.mabini.meditrack.viewmodel.RecordUiState
import edu.cit.mabini.meditrack.feature.medicalrecords.MedicalRecordViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen(
    patientId: Long,
    viewModel: MedicalRecordViewModel,
    userRole: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val isStaff = userRole == "SUPER_ADMIN" || userRole == "NURSE"

    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(patientId) {
        viewModel.loadRecords(patientId)
    }

    LaunchedEffect(actionState) {
        if (actionState is RecordActionState.Success) {
            showAddDialog = false
            snackbarHostState.showSnackbar("Record saved")
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isStaff) "Medical Records" else "My Health History", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Only staff can add records
                    if (isStaff) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Record", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF161B22))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is RecordUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2196F3))
                }
                is RecordUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text((uiState as RecordUiState.Error).message, color = Color(0xFFEF5350))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadRecords(patientId) }) {
                            Text("Retry")
                        }
                    }
                }
                is RecordUiState.Success -> {
                    val records = (uiState as RecordUiState.Success).records
                    if (records.isEmpty()) {
                        Text("No records found for this patient", color = Color(0xFF8B949E), modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(records) { record ->
                                MedicalRecordCard(record)
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        if (showAddDialog) {
            AddRecordDialog(
                actionState = actionState,
                onDismiss = { showAddDialog = false },
                onConfirm = { record -> viewModel.createRecord(record.copy(patientId = patientId)) }
            )
        }
    }
}

@Composable
fun MedicalRecordCard(record: MedicalRecordDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val formattedDate = try {
                val date = LocalDate.parse(record.visitDate)
                date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            } catch (e: Exception) {
                record.visitDate ?: "N/A"
            }

            Text(
                text = formattedDate,
                color = Color(0xFF2196F3),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(record.diagnosis ?: "No diagnosis", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            
            if (!record.treatment.isNullOrBlank()) {
                LabeledText("Treatment", record.treatment)
            }
            if (!record.prescription.isNullOrBlank()) {
                LabeledText("Rx", record.prescription, valueColor = Color(0xFF2196F3))
            }
            if (!record.notes.isNullOrBlank()) {
                Text(record.notes, color = Color(0xFF8B949E), fontSize = 13.sp, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
fun LabeledText(label: String, value: String, valueColor: Color = Color.White) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", color = Color(0xFF8B949E), fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    actionState: RecordActionState,
    onDismiss: () -> Unit,
    onConfirm: (MedicalRecordDto) -> Unit
) {
    var diagnosis by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var prescription by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf(LocalDate.now().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medical Record", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                DialogField("Diagnosis*", diagnosis) { diagnosis = it }
                DialogField("Treatment", treatment, singleLine = false) { treatment = it }
                DialogField("Prescription", prescription, singleLine = false) { prescription = it }
                DialogField("Notes", notes, singleLine = false) { notes = it }
                DialogField("Visit Date*", visitDate, placeholder = "yyyy-MM-dd") { visitDate = it }

                if (actionState is RecordActionState.Error) {
                    Text(actionState.message, color = Color(0xFFEF5350), fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (diagnosis.isNotBlank() && visitDate.isNotBlank()) {
                        onConfirm(MedicalRecordDto(
                            diagnosis = diagnosis,
                            treatment = treatment.ifBlank { null },
                            prescription = prescription.ifBlank { null },
                            notes = notes.ifBlank { null },
                            visitDate = visitDate
                        ))
                    }
                },
                enabled = actionState !is RecordActionState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                if (actionState is RecordActionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF8B949E))
            }
        },
        containerColor = Color(0xFF161B22)
    )
}
