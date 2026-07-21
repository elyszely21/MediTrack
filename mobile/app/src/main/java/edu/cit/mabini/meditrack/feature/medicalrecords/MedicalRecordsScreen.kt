package edu.cit.mabini.meditrack.feature.medicalrecords

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.mabini.meditrack.feature.medicalrecords.data.model.MedicalRecordDto
import edu.cit.mabini.meditrack.feature.medicalrecords.RecordActionState
import edu.cit.mabini.meditrack.feature.medicalrecords.RecordUiState
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
    val isStaff = userRole == "SUPER_ADMIN" || userRole == "NURSE" || userRole == "DOCTOR"

    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(patientId) {
        viewModel.loadRecords(patientId)
    }

    LaunchedEffect(actionState) {
        if (actionState is RecordActionState.Success) {
            showAddDialog = false
            snackbarHostState.showSnackbar("Record saved successfully")
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(if (isStaff) "Medical Records" else "My Health History", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                        if (isStaff && patientId != 0L) {
                            Text("Viewing patient history", color = Color(0xFF8B949E), fontSize = 12.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isStaff && patientId != 0L) {
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
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📭", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isStaff) "No records found for this patient" else "No medical records yet",
                                color = Color(0xFF8B949E)
                            )
                            if (isStaff && patientId != 0L) {
                                Button(
                                    onClick = { showAddDialog = true },
                                    modifier = Modifier.padding(top = 16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                ) {
                                    Text("Add First Record")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = record.diagnosis ?: "No diagnosis",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "📅 Visit Date: ${record.visitDate ?: "—"}",
                        color = Color(0xFF2196F3),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = "Record #${record.id}",
                    color = Color(0xFF8B949E),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!record.treatment.isNullOrBlank()) {
                    RecordSection("Treatment", record.treatment, Color(0xFF8B949E).copy(alpha = 0.1f))
                }
                if (!record.prescription.isNullOrBlank()) {
                    RecordSection("Prescription", record.prescription, Color(0xFF2196F3).copy(alpha = 0.1f), Color(0xFF2196F3))
                }
                if (!record.notes.isNullOrBlank()) {
                    RecordSection("Notes", record.notes, Color(0xFFF9A825).copy(alpha = 0.05f), labelColor = Color(0xFFF9A825), isItalic = true)
                }
            }
        }
    }
}

@Composable
fun RecordSection(
    label: String,
    content: String,
    bgColor: Color,
    labelColor: Color = Color(0xFF8B949E),
    isItalic: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, color = labelColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                text = content,
                color = Color.White,
                fontSize = 13.sp,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
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
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medical Record", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                RecordFormField("Visit Date *", visitDate, placeholder = "yyyy-MM-dd") { visitDate = it }
                RecordFormField("Diagnosis *", diagnosis) { diagnosis = it }
                RecordFormField("Treatment", treatment, singleLine = false) { treatment = it }
                RecordFormField("Prescription", prescription, singleLine = false) { prescription = it }
                RecordFormField("Notes", notes, singleLine = false) { notes = it }

                if (error.isNotEmpty()) {
                    Text(error, color = Color(0xFFEF5350), fontSize = 12.sp)
                }
                if (actionState is RecordActionState.Error) {
                    Text(actionState.message, color = Color(0xFFEF5350), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (diagnosis.isBlank() || visitDate.isBlank()) {
                        error = "Required fields missing"
                    } else {
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
                    Text("Save Record")
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

@Composable
fun RecordFormField(
    label: String,
    value: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF8B949E))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF484F58)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color(0xFF30363D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF21262D),
                unfocusedContainerColor = Color(0xFF21262D)
            ),
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else 5
        )
    }
}
