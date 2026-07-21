package edu.cit.mabini.meditrack.feature.appointments

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.mabini.meditrack.feature.appointments.data.model.AppointmentDto
import edu.cit.mabini.meditrack.feature.doctors.data.model.DoctorDto
import edu.cit.mabini.meditrack.feature.appointments.AppointmentActionState
import edu.cit.mabini.meditrack.feature.appointments.AppointmentUiState
import edu.cit.mabini.meditrack.feature.appointments.AppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    viewModel: AppointmentViewModel,
    userRole: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val isAdmin = userRole == "SUPER_ADMIN"
    val isStaff = userRole == "SUPER_ADMIN" || userRole == "NURSE" || userRole == "DOCTOR"

    var showAddDialog by remember { mutableStateOf(false) }
    var showActionDialog by remember { mutableStateOf<Pair<AppointmentDto, String>?>(null) }
    var actionReason by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAppointments(isStaff)
    }

    LaunchedEffect(actionState) {
        if (actionState is AppointmentActionState.Success) {
            showAddDialog = false
            showActionDialog = null
            actionReason = ""
            snackbarHostState.showSnackbar("Action successful")
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isStaff) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Schedule Appointment", tint = Color.White)
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
            // Status Filters - Replicating Tab behavior from React
            val statuses = listOf("ALL", "REQUESTED", "PENDING_APPROVAL", "APPROVED", "CHECKED_IN", "WAITING", "IN_CONSULTATION", "PRESCRIPTION_ISSUED", "COMPLETED", "NO_SHOW", "CANCELLED", "REJECTED")
            ScrollableTabRow(
                selectedTabIndex = statuses.indexOf(statusFilter).coerceAtLeast(0),
                containerColor = Color(0xFF161B22),
                contentColor = Color(0xFF2196F3),
                edgePadding = 16.dp,
                divider = {}
            ) {
                statuses.forEach { status ->
                    Tab(
                        selected = statusFilter == status,
                        onClick = { viewModel.setStatusFilter(status, isStaff) },
                        text = { 
                            Text(
                                text = status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 13.sp,
                                fontWeight = if (statusFilter == status) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        selectedContentColor = Color(0xFF2196F3),
                        unselectedContentColor = Color(0xFF8B949E)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState) {
                    is AppointmentUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2196F3))
                    }
                    is AppointmentUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text((uiState as AppointmentUiState.Error).message, color = Color(0xFFEF5350))
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadAppointments(isStaff) }) {
                                Text("Retry")
                            }
                        }
                    }
                    is AppointmentUiState.Success -> {
                        val appointments = (uiState as AppointmentUiState.Success).appointments
                        
                        if (appointments.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📅", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No appointments found", color = Color(0xFF8B949E))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(appointments) { appointment ->
                                    AppointmentCard(
                                        appointment = appointment,
                                        isAdmin = isAdmin,
                                        onAction = { action -> showActionDialog = appointment to action }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

        if (showAddDialog) {
            AddAppointmentDialog(
                viewModel = viewModel,
                isStaff = isStaff,
                onDismiss = { showAddDialog = false }
            )
        }

        if (showActionDialog != null) {
            val (appointment, action) = showActionDialog!!
            ActionConfirmationDialog(
                appointment = appointment,
                action = action,
                reason = actionReason,
                onReasonChange = { actionReason = it },
                onDismiss = { 
                    showActionDialog = null
                    actionReason = ""
                },
                onConfirm = { 
                    viewModel.performAction(appointment.id ?: 0, action, isStaff, if (actionReason.isBlank()) null else actionReason)
                },
                isSubmitting = actionState is AppointmentActionState.Loading
            )
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentDto,
    isAdmin: Boolean,
    onAction: (String) -> Unit
) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${appointment.appointmentDate} • ${appointment.appointmentTime?.take(5)}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = appointment.patientName ?: "Patient #${appointment.patientId}",
                        color = Color(0xFF2196F3),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("No: ${appointment.patientNumber ?: "N/A"}", color = Color(0xFF8B949E), fontSize = 12.sp)
                    if (appointment.remarks.isNotBlank()) {
                        Text("Remarks: ${appointment.remarks}", color = Color(0xFF8B949E), fontSize = 12.sp, maxLines = 2)
                    }
                }
                StatusChip(appointment.status)
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF30363D))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (appointment.status) {
                        "REQUESTED", "PENDING_APPROVAL" -> {
                            ActionButton("Approve", Color(0xFF2E7D32)) { onAction("approve") }
                            ActionButton("Reject", Color(0xFFC62828)) { onAction("reject") }
                        }
                        "APPROVED" -> {
                            ActionButton("Check-in", Color(0xFF3949AB)) { onAction("check-in") }
                            ActionButton("Cancel", Color(0xFFF9A825)) { onAction("cancel") }
                        }
                        "CHECKED_IN" -> {
                            ActionButton("Waiting", Color(0xFF7B1FA2)) { onAction("waiting") }
                        }
                        "WAITING" -> {
                            ActionButton("Start Consult", Color(0xFF00ACC1)) { onAction("in-consultation") }
                            ActionButton("No-show", Color(0xFF757575)) { onAction("no-show") }
                        }
                        "IN_CONSULTATION" -> {
                            ActionButton("Prescription", Color(0xFF00897B)) { onAction("prescription-issued") }
                            ActionButton("Complete", Color(0xFF2E7D32)) { onAction("complete") }
                        }
                        "PRESCRIPTION_ISSUED" -> {
                            ActionButton("Complete", Color(0xFF2E7D32)) { onAction("complete") }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onAction("delete") }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f), contentColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusChip(status: String?) {
    val statusMap = mapOf(
        "REQUESTED" to (Color(0xFFFFF9C4) to Color(0xFFF57F17)),
        "PENDING_APPROVAL" to (Color(0xFFE3F2FD) to Color(0xFF0D47A1)),
        "APPROVED" to (Color(0xFFE3F2FD) to Color(0xFF2196F3)),
        "CHECKED_IN" to (Color(0xFFE8EAF6) to Color(0xFF3F51B5)),
        "WAITING" to (Color(0xFFF3E5F5) to Color(0xFF9C27B0)),
        "IN_CONSULTATION" to (Color(0xFFE0F7FA) to Color(0xFF00BCD4)),
        "PRESCRIPTION_ISSUED" to (Color(0xFFE0F2F1) to Color(0xFF009688)),
        "COMPLETED" to (Color(0xFFE8F5E9) to Color(0xFF4CAF50)),
        "NO_SHOW" to (Color(0xFFF5F5F5) to Color(0xFF616161)),
        "CANCELLED" to (Color(0xFFF5F5F5) to Color(0xFF616161)),
        "REJECTED" to (Color(0xFFFFEBEE) to Color(0xFFF44336))
    )

    val (bgColor, textColor) = statusMap[status] ?: (Color(0xFF30363D) to Color(0xFF8B949E))

    Surface(
        color = bgColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, bgColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = status?.replace("_", " ") ?: "UNKNOWN",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentDialog(
    viewModel: AppointmentViewModel,
    isStaff: Boolean,
    onDismiss: () -> Unit
) {
    val actionState by viewModel.actionState.collectAsState()
    val doctors by viewModel.doctors.collectAsState()

    var patientNumber by remember { mutableStateOf("") }
    var doctorId by remember { mutableStateOf<Long?>(null) }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDoctors()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Appointment", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                DialogField("Patient Number *", patientNumber, placeholder = "e.g. PT-00001") { patientNumber = it }
                
                // Doctor selection
                Text("Doctor *", fontSize = 12.sp, color = Color(0xFF8B949E))
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    val selectedDoctorName = doctors.find { it.id == doctorId }?.fullName ?: "Select a doctor"
                    OutlinedTextField(
                        value = selectedDoctorName,
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
                        doctors.forEach { doctor ->
                            DropdownMenuItem(
                                text = { Text(doctor.fullName, color = Color.White) },
                                onClick = {
                                    doctorId = doctor.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                DialogField("Date *", date, placeholder = "yyyy-MM-dd") { date = it }
                DialogField("Time *", time, placeholder = "HH:mm") { time = it }
                DialogField("Remarks", remarks, singleLine = false) { remarks = it }

                if (error.isNotBlank()) {
                    Text(error, color = Color(0xFFEF5350), fontSize = 12.sp)
                }
                if (actionState is AppointmentActionState.Error) {
                    Text((actionState as AppointmentActionState.Error).message, color = Color(0xFFEF5350), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (patientNumber.isBlank() || doctorId == null || date.isBlank() || time.isBlank()) {
                        error = "Please fill all required fields"
                        return@Button
                    }
                    error = ""
                    viewModel.lookupPatient(patientNumber) { patient ->
                        if (patient == null) {
                            error = "Patient not found"
                        } else {
                            val durationMinutes = 30
                            val formattedTime = if (time.length == 5) "$time:00" else time
                            // Calculate end time
                            val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
                            val startTimeDate = try { sdf.parse(formattedTime) } catch (e: Exception) { null }
                            if (startTimeDate != null) {
                                val calendar = java.util.Calendar.getInstance()
                                calendar.time = startTimeDate
                                calendar.add(java.util.Calendar.MINUTE, durationMinutes)
                                val endTime = sdf.format(calendar.time)

                                viewModel.createAppointment(AppointmentDto(
                                    patientId = patient.id,
                                    doctorId = doctorId!!,
                                    appointmentDate = date,
                                    appointmentTime = formattedTime,
                                    endTime = endTime,
                                    appointmentType = "CONSULTATION",
                                    durationMinutes = durationMinutes,
                                    remarks = if (remarks.isBlank()) " — " else remarks,
                                    status = "REQUESTED"
                                ), isStaff)
                            } else {
                                error = "Invalid time format"
                            }
                        }
                    }
                },
                enabled = actionState !is AppointmentActionState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                if (actionState is AppointmentActionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Schedule")
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
fun ActionConfirmationDialog(
    appointment: AppointmentDto,
    action: String,
    reason: String,
    onReasonChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isSubmitting: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${action.replaceFirstChar { it.uppercase() }} Appointment", color = Color.White) },
        text = {
            Column {
                Text(
                    "Are you sure you want to $action the appointment for ${appointment.patientName}?",
                    color = Color(0xFF8B949E)
                )
                if (action == "reject" || action == "cancel" || action == "no-show") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = onReasonChange,
                        label = { Text("Reason (required)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color(0xFF30363D),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF21262D),
                            unfocusedContainerColor = Color(0xFF21262D)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting && (if (action == "reject" || action == "cancel" || action == "no-show") reason.isNotBlank() else true),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when(action) {
                        "reject", "delete" -> Color(0xFFC62828)
                        "cancel", "no-show" -> Color(0xFFF9A825)
                        else -> Color(0xFF2196F3)
                    }
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(action.replaceFirstChar { it.uppercase() })
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
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF8B949E))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFF484F58)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color(0xFF30363D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF21262D),
                unfocusedContainerColor = Color(0xFF21262D)
            )
        )
    }
}
