package edu.cit.mabini.meditrack.feature.appointments

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.mabini.meditrack.feature.appointments.data.model.AppointmentDto
import edu.cit.mabini.meditrack.viewmodel.AppointmentActionState
import edu.cit.mabini.meditrack.viewmodel.AppointmentUiState
import edu.cit.mabini.meditrack.feature.appointments.AppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    viewModel: AppointmentViewModel,
    userRole: String,
    userId: Long,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val isStaff = userRole == "SUPER_ADMIN" || userRole == "NURSE"

    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAppointments()
    }

    LaunchedEffect(actionState) {
        if (actionState is AppointmentActionState.Success) {
            showAddDialog = false
            snackbarHostState.showSnackbar("Appointment scheduled")
            viewModel.resetActionState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isStaff) "Appointments" else "My Appointments", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Book Appointment", tint = Color.White)
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
                        Button(onClick = { viewModel.loadAppointments() }) {
                            Text("Retry")
                        }
                    }
                }
                is AppointmentUiState.Success -> {
                    val allAppointments = (uiState as AppointmentUiState.Success).appointments
                    // If not staff, only show appointments belonging to this user
                    val appointments = if (isStaff) allAppointments else allAppointments.filter { it.patientId == userId }
                    
                    if (appointments.isEmpty()) {
                        Text("No appointments found", color = Color(0xFF8B949E), modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(appointments) { appointment ->
                                AppointmentCard(appointment)
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        if (showAddDialog) {
            AddAppointmentDialog(
                actionState = actionState,
                isStaff = isStaff,
                currentUserId = userId,
                onDismiss = { showAddDialog = false },
                onConfirm = { appointment -> viewModel.createAppointment(appointment) }
            )
        }
    }
}

@Composable
fun AppointmentCard(appointment: AppointmentDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${appointment.appointmentDate} at ${appointment.appointmentTime?.take(5)}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Patient ID: ${appointment.patientId}", color = Color(0xFF8B949E), fontSize = 13.sp)
                    if (!appointment.remarks.isNullOrBlank()) {
                        Text(appointment.remarks, color = Color(0xFF8B949E), fontSize = 12.sp, maxLines = 2)
                    }
                }
                StatusChip(appointment.status)
            }
        }
    }
}

@Composable
fun StatusChip(status: String?) {
    val (bgColor, textColor) = when (status) {
        "SCHEDULED" -> Color(0xFF1565C0) to Color(0xFFBBDEFB)
        "COMPLETED" -> Color(0xFF1B5E20) to Color(0xFFC8E6C9)
        "CANCELLED" -> Color(0xFFB71C1C) to Color(0xFFFFCDD2)
        else -> Color(0xFF30363D) to Color(0xFF8B949E)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = status ?: "UNKNOWN",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentDialog(
    actionState: AppointmentActionState,
    isStaff: Boolean,
    currentUserId: Long,
    onDismiss: () -> Unit,
    onConfirm: (AppointmentDto) -> Unit
) {
    var patientId by remember { mutableStateOf(if (isStaff) "" else currentUserId.toString()) }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("SCHEDULED") }
    var remarks by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("SCHEDULED", "COMPLETED", "CANCELLED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isStaff) "Schedule Appointment" else "Book Appointment", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Only show Patient ID input for Staff
                if (isStaff) {
                    DialogField("Patient ID*", patientId, keyboardType = KeyboardType.Number) { patientId = it }
                }

                DialogField("Date*", date, placeholder = "yyyy-MM-dd") { date = it }
                DialogField("Time*", time, placeholder = "HH:mm e.g. 09:00") { time = it }

                if (isStaff) {
                    Text("Status", fontSize = 12.sp, color = Color(0xFF8B949E))
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = status,
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
                            statuses.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = Color.White) },
                                    onClick = {
                                        status = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                DialogField("Remarks", remarks, singleLine = false) { remarks = it }

                if (actionState is AppointmentActionState.Error) {
                    Text(actionState.message, color = Color(0xFFEF5350), fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pId = patientId.toLongOrNull()
                    if (pId != null && date.isNotBlank() && time.isNotBlank()) {
                        val formattedTime = if (time.length == 5) "$time:00" else time
                        onConfirm(AppointmentDto(
                            patientId = pId,
                            appointmentDate = date,
                            appointmentTime = formattedTime,
                            status = status,
                            remarks = remarks
                        ))
                    }
                },
                enabled = actionState !is AppointmentActionState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                if (actionState is AppointmentActionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(if (isStaff) "Save" else "Book Now")
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
