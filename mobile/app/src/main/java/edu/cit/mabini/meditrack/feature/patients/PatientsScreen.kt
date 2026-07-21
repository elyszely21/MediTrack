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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.mabini.meditrack.feature.patients.data.model.PatientDto
import edu.cit.mabini.meditrack.feature.patients.PatientActionState
import edu.cit.mabini.meditrack.feature.patients.PatientUiState
import edu.cit.mabini.meditrack.feature.patients.PatientViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    viewModel: PatientViewModel,
    userRole: String,
    onBack: () -> Unit,
    onViewRecords: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val isAdmin = userRole == "SUPER_ADMIN"

    var searchQuery by remember { mutableStateOf("") }
    var showArchived by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<PatientDto?>(null) }
    var archiveTarget by remember { mutableStateOf<PatientDto?>(null) }
    var deleteTarget by remember { mutableStateOf<PatientDto?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(searchQuery, showArchived) {
        delay(300)
        viewModel.loadPatients(userRole, searchQuery, showArchived)
    }

    LaunchedEffect(actionState) {
        if (actionState is PatientActionState.Success) {
            showForm = false
            editTarget = null
            archiveTarget = null
            deleteTarget = null
            snackbarHostState.showSnackbar("Action successful")
            viewModel.resetActionState()
            viewModel.loadPatients(userRole, searchQuery, showArchived)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Patients", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                        Text(
                            text = if (showArchived) "Showing archived patients" else "Showing active patients",
                            color = Color(0xFF8B949E),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { 
                            editTarget = null
                            showForm = true 
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Patient", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF161B22))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0D1117)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Search and Filter Bar
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or number...", color = Color(0xFF8B949E)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B949E)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF8B949E))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFF30363D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF21262D),
                        unfocusedContainerColor = Color(0xFF21262D)
                    ),
                    singleLine = true
                )
                
                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showArchived = !showArchived },
                        modifier = Modifier
                            .background(
                                if (showArchived) Color(0xFFF9A825).copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            if (showArchived) Icons.Default.Inventory2 else Icons.Default.Groups,
                            contentDescription = "Toggle Archived",
                            tint = if (showArchived) Color(0xFFF9A825) else Color(0xFF8B949E)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                            Button(onClick = { viewModel.loadPatients(userRole, searchQuery, showArchived) }) {
                                Text("Retry")
                            }
                        }
                    }
                    is PatientUiState.Success -> {
                        val patients = (uiState as PatientUiState.Success).patients
                        if (patients.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "🔍" else if (showArchived) "📦" else "👥",
                                    fontSize = 48.sp
                                )
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No patients found for \"$searchQuery\""
                                           else if (showArchived) "No archived patients"
                                           else "No patients registered yet",
                                    color = Color(0xFF8B949E),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(patients) { patient ->
                                    PatientCard(
                                        patient = patient,
                                        isAdmin = isAdmin,
                                        onView = { patient.id?.let { onViewRecords(it) } },
                                        onEdit = { 
                                            editTarget = patient
                                            showForm = true 
                                        },
                                        onArchive = { archiveTarget = patient },
                                        onDelete = { deleteTarget = patient }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        if (showForm) {
            PatientFormDialog(
                patient = editTarget,
                actionState = actionState,
                onDismiss = { 
                    showForm = false
                    editTarget = null
                },
                onConfirm = { patient -> 
                    if (editTarget != null) {
                        viewModel.updatePatient(userRole, editTarget!!.id!!, patient)
                    } else {
                        viewModel.createPatient(userRole, patient)
                    }
                }
            )
        }

        if (archiveTarget != null) {
            AlertDialog(
                onDismissRequest = { archiveTarget = null },
                title = { Text(if (archiveTarget!!.archived) "Restore Patient" else "Archive Patient", color = Color.White) },
                text = {
                    Text(
                        if (archiveTarget!!.archived) "Restore ${archiveTarget!!.fullName} to active patients?"
                        else "Archive ${archiveTarget!!.fullName}? Their records will be preserved.",
                        color = Color(0xFF8B949E)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.archivePatient(userRole, archiveTarget!!.id!!, archiveTarget!!.archived) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (archiveTarget!!.archived) Color(0xFF2E7D32) else Color(0xFFF9A825)
                        )
                    ) {
                        Text(if (archiveTarget!!.archived) "Restore" else "Archive")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { archiveTarget = null }) {
                        Text("Cancel", color = Color(0xFF8B949E))
                    }
                },
                containerColor = Color(0xFF161B22)
            )
        }

        if (deleteTarget != null) {
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("Permanently Delete Patient", color = Color(0xFFEF5350)) },
                text = {
                    Column {
                        Surface(
                            color = Color(0xFFEF5350).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                "⚠️ This action cannot be undone. All records, appointments, and bills will also be deleted.",
                                modifier = Modifier.padding(8.dp),
                                color = Color(0xFFEF5350),
                                fontSize = 12.sp
                            )
                        }
                        Text("Permanently delete ${deleteTarget!!.fullName} (${deleteTarget!!.patientNumber})?", color = Color(0xFF8B949E))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.deletePatient(userRole, deleteTarget!!.id!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTarget = null }) {
                        Text("Cancel", color = Color(0xFF8B949E))
                    }
                },
                containerColor = Color(0xFF161B22)
            )
        }
    }
}

@Composable
fun PatientCard(
    patient: PatientDto,
    isAdmin: Boolean,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161B22)
        ),
        border = if (patient.archived) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30363D))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .alpha(if (patient.archived) 0.6f else 1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF1565C0),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = patient.patientNumber,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(patient.fullName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                if (patient.archived) {
                    StatusLabel("Archived", Color(0xFFF9A825))
                } else {
                    StatusLabel("Active", Color(0xFF2E7D32))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Gender", patient.gender ?: "—", Modifier.weight(1f))
                InfoItem("Birth Date", patient.birthDate ?: "—", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem("Contact", patient.contactNumber ?: "—", Modifier.weight(1f))
                InfoItem("Address", patient.address ?: "—", Modifier.weight(1f))
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF30363D))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!patient.archived) {
                        Text(
                            "Edit",
                            color = Color(0xFF2196F3),
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onEdit() }.padding(8.dp)
                        )
                    }
                    Text(
                        if (patient.archived) "Restore" else "Archive",
                        color = if (patient.archived) Color(0xFF4CAF50) else Color(0xFFF9A825),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onArchive() }.padding(8.dp)
                    )
                    Text(
                        "Delete",
                        color = Color(0xFFEF5350),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onDelete() }.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusLabel(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = Color(0xFF8B949E), fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 13.sp, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFormDialog(
    patient: PatientDto?,
    actionState: PatientActionState,
    onDismiss: () -> Unit,
    onConfirm: (PatientDto) -> Unit
) {
    var patientNumber by remember { mutableStateOf(patient?.patientNumber ?: "") }
    var firstName by remember { mutableStateOf(patient?.firstName ?: "") }
    var lastName by remember { mutableStateOf(patient?.lastName ?: "") }
    var gender by remember { mutableStateOf(patient?.gender ?: "") }
    var birthDate by remember { mutableStateOf(patient?.birthDate ?: "") }
    var contactNumber by remember { mutableStateOf(patient?.contactNumber ?: "") }
    var emergencyContact by remember { mutableStateOf(patient?.emergencyContact ?: "") }
    var address by remember { mutableStateOf(patient?.address ?: "") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (patient != null) "Edit Patient" else "Register Patient", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                PatientFormField("Patient Number *", patientNumber, enabled = patient == null) { patientNumber = it }
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) { PatientFormField("First Name *", firstName) { firstName = it } }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f)) { PatientFormField("Last Name *", lastName) { lastName = it } }
                }
                
                Text("Gender", fontSize = 12.sp, color = Color(0xFF8B949E))
                Spacer(Modifier.height(4.dp))
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
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
                        listOf("Male", "Female", "Other").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.White) },
                                onClick = { gender = option; expanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                PatientFormField("Birth Date", birthDate, placeholder = "yyyy-MM-dd") { birthDate = it }
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) { PatientFormField("Contact Number", contactNumber) { contactNumber = it } }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f)) { PatientFormField("Emergency Contact", emergencyContact) { emergencyContact = it } }
                }
                PatientFormField("Address", address) { address = it }

                if (error.isNotEmpty()) {
                    Text(error, color = Color(0xFFEF5350), fontSize = 12.sp)
                }
                if (actionState is PatientActionState.Error) {
                    Text(actionState.message, color = Color(0xFFEF5350), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (patientNumber.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                        error = "Required fields missing"
                    } else {
                        onConfirm(PatientDto(
                            patientNumber = patientNumber,
                            firstName = firstName,
                            lastName = lastName,
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
                    Text(if (patient != null) "Update" else "Register")
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
fun PatientFormField(
    label: String,
    value: String,
    placeholder: String = "",
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF8B949E))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF484F58)) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color(0xFF30363D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF21262D),
                unfocusedContainerColor = Color(0xFF21262D),
                disabledContainerColor = Color(0xFF21262D).copy(alpha = 0.5f)
            ),
            singleLine = true
        )
    }
}
