package edu.cit.mabini.meditrack.feature.patients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientDto
import edu.cit.mabini.meditrack.feature.patients.PatientActionState
import edu.cit.mabini.meditrack.feature.patients.PatientUiState
import edu.cit.mabini.meditrack.feature.patients.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    viewModel: PatientViewModel,
    onBack: () -> Unit,
    onViewRecords: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadPatients()
    }

    LaunchedEffect(actionState) {
        if (actionState is PatientActionState.Success) {
            showAddDialog = false
            snackbarHostState.showSnackbar("Patient added")
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Patient", tint = Color.White)
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
                is PatientUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2196F3))
                }
                is PatientUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text((uiState as PatientUiState.Error).message, color = Color(0xFFEF5350))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadPatients() }) {
                            Text("Retry")
                        }
                    }
                }
                is PatientUiState.Success -> {
                    val patients = (uiState as PatientUiState.Success).patients
                    if (patients.isEmpty()) {
                        Text("No patients found", color = Color(0xFF8B949E), modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(patients) { patient ->
                                PatientCard(patient, onViewRecords)
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        if (showAddDialog) {
            AddPatientDialog(
                actionState = actionState,
                onDismiss = { showAddDialog = false },
                onConfirm = { patient -> viewModel.createPatient(patient) }
            )
        }
    }
}

@Composable
fun PatientCard(patient: PatientDto, onClick: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { patient.id?.let { onClick(it) } },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF1565C0),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = patient.patientNumber,
                            color = Color(0xFFBBDEFB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(patient.fullName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("${patient.gender ?: "—"}  •  ${patient.birthDate ?: "—"}", color = Color(0xFF8B949E), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(patient.contactNumber ?: "No contact", color = Color(0xFF8B949E), fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8B949E))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientDialog(
    actionState: PatientActionState,
    onDismiss: () -> Unit,
    onConfirm: (PatientDto) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var patientNumber by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    val genders = listOf("Male", "Female", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Patient", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                DialogField("First Name*", firstName) { firstName = it }
                DialogField("Last Name*", lastName) { lastName = it }
                DialogField("Patient Number*", patientNumber, placeholder = "P-0001") { patientNumber = it }
                
                Text("Gender", fontSize = 12.sp, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color(0xFF30363D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF21262D),
                            unfocusedContainerColor = Color(0xFF21262D)
                        ),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF161B22))
                    ) {
                        genders.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.White) },
                                onClick = {
                                    gender = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                DialogField("Birth Date", birthDate, placeholder = "yyyy-MM-dd") { birthDate = it }
                DialogField("Contact Number", contactNumber, keyboardType = KeyboardType.Phone) { contactNumber = it }
                DialogField("Emergency Contact", emergencyContact, keyboardType = KeyboardType.Phone) { emergencyContact = it }
                DialogField("Address", address, singleLine = false) { address = it }

                if (actionState is PatientActionState.Error) {
                    Text(actionState.message, color = Color(0xFFEF5350), fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank() && patientNumber.isNotBlank()) {
                        onConfirm(PatientDto(
                            firstName = firstName,
                            lastName = lastName,
                            patientNumber = patientNumber,
                            gender = gender,
                            birthDate = birthDate,
                            contactNumber = contactNumber,
                            emergencyContact = emergencyContact,
                            address = address
                        ))
                    }
                },
                enabled = actionState !is PatientActionState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                if (actionState is PatientActionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Add Patient")
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
fun DialogField(
    label: String,
    value: String,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Text(label, fontSize = 12.sp, color = Color(0xFF8B949E))
    Spacer(modifier = Modifier.height(4.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF8B949E).copy(alpha = 0.5f)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2196F3),
            unfocusedBorderColor = Color(0xFF30363D),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0xFF21262D),
            unfocusedContainerColor = Color(0xFF21262D)
        ),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 3
    )
    Spacer(modifier = Modifier.height(12.dp))
}
