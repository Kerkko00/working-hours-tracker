package com.kerkko00.workinghourstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.kerkko00.workinghourstracker.ui.theme.WorkingHoursTrackerTheme
import com.google.gson.Gson
import java.io.File
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkingHoursTrackerTheme {
                WorkingHoursTrackerApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun WorkingHoursTrackerApp() {
    val context = LocalContext.current
    val dataFile = remember { File(context.filesDir, "events.json") }
    val settingsFile = remember { File(context.filesDir, "settings.json") }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val eventTracker = remember { EventTracker() }
    val events = remember { mutableStateListOf<TrackedEvent>() }
    var weeklyThreshold by remember { mutableDoubleStateOf(getDefaultWeeklyThreshold()) }
    var pastWeeksToCheck by remember { mutableIntStateOf(getDefaultPastWeeksToCheck()) }
    val gson = remember { Gson() }

    // Helper to refresh tracker events and save to file
    fun refreshEventsAndSave() {
        events.clear()
        events.addAll(eventTracker.getEvents().sortedByDescending { it.date })
        eventTracker.saveToFile(dataFile)
    }

    LaunchedEffect(Unit) {
        // Load events
        eventTracker.loadFromFile(dataFile)
        events.clear()
        events.addAll(eventTracker.getEvents().sortedByDescending { it.date })

        // Load settings
        if (settingsFile.exists()) {
            val content = settingsFile.readText()
            try {
                val settings = gson.fromJson(content, AppSettings::class.java)
                weeklyThreshold = settings.weeklyThreshold
                pastWeeksToCheck = settings.pastWeeksToCheck
            } catch (e: Exception) {
                println("Error loading settings: $e")
                weeklyThreshold = getDefaultWeeklyThreshold()
                pastWeeksToCheck = getDefaultPastWeeksToCheck()
            }
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.HOME -> {
                // Fetch data from tracker reactively based on events list size
                val exceedingWeeks = events.size.let { 
                    eventTracker.getExceedingWeeks(LocalDate.now(), pastWeeksToCheck, weeklyThreshold)
                }
                
                HomeScreen(
                    events = events,
                    exceedingWeeks = exceedingWeeks,
                    weeklyThreshold = weeklyThreshold,
                    onAddEvent = { date, hours ->
                        eventTracker.addEvent(date, hours)
                        refreshEventsAndSave()
                    },
                    onDeleteEvent = { date ->
                        eventTracker.removeEvent(date)
                        refreshEventsAndSave()
                    },
                    onEditEvent = { date, hours ->
                        eventTracker.editEvent(date, hours)
                        refreshEventsAndSave()
                    }
                )
            }
            AppDestinations.SETTINGS -> SettingsScreen(
                initialWeeklyHourLimit = weeklyThreshold,
                initialPastWeeksToCheck = pastWeeksToCheck,
                onSaveSettings = { newThreshold, newPastWeeks ->
                    weeklyThreshold = newThreshold
                    pastWeeksToCheck = newPastWeeks
                    val settings = AppSettings(newThreshold, newPastWeeks)
                    settingsFile.writeText(gson.toJson(settings))
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    events: List<TrackedEvent>,
    exceedingWeeks: List<Pair<LocalDate, Double>>,
    weeklyThreshold: Double,
    onAddEvent: (LocalDate, Double) -> Unit,
    onDeleteEvent: (LocalDate) -> Unit,
    onEditEvent: (LocalDate, Double) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var hoursString by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val currentWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Working Hours Tracker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (exceedingWeeks.isNotEmpty()) {
            Card(
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add, // Using Add as a filler, ideally use Warning or similar
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Weekly limit exceeded!",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.padding(vertical = 4.dp))
                    exceedingWeeks.forEach { (monday, total) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                        ) {
                            val label = if (monday == currentWeekMonday) "Current Week" else "Week of ${monday.format(DateTimeFormatter.ofPattern("MMM d"))}"
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "$total / $weeklyThreshold hrs",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        value = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        onValueChange = { },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false // Disable interaction with the text field itself to ensure the Box click works
                    )
                }
                OutlinedTextField(
                    value = hoursString,
                    onValueChange = { hoursString = it },
                    label = { Text("Hours") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val hours = hoursString.toDoubleOrNull() ?: 0.0
                        onAddEvent(selectedDate, hours)
                        hoursString = ""
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Entry")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "No entries yet. Add your first one above!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn {
                items(events) { event ->
                    EventItem(
                        event = event,
                        onDelete = { onDeleteEvent(event.date) },
                        onEdit = { hours -> onEditEvent(event.date, hours) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventItem(
    event: TrackedEvent,
    onDelete: () -> Unit,
    onEdit: (Double) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editHoursString by remember { mutableStateOf(event.trackedHours.toString()) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Hours") },
            text = {
                OutlinedTextField(
                    value = editHoursString,
                    onValueChange = { editHoursString = it },
                    label = { Text("Hours") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val hours = editHoursString.toDoubleOrNull() ?: event.trackedHours
                    onEdit(hours)
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete the entry for ${event.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = event.weekDay.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "${event.trackedHours} hrs",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun SettingsScreen(
    initialWeeklyHourLimit: Double,
    initialPastWeeksToCheck: Int,
    onSaveSettings: (Double, Int) -> Unit
) {
    var weeklyHourLimitString by remember { mutableStateOf(initialWeeklyHourLimit.toString()) }
    var pastWeeksToCheckString by remember { mutableStateOf(initialPastWeeksToCheck.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = weeklyHourLimitString,
                    onValueChange = { weeklyHourLimitString = it },
                    label = { Text("Weekly hour limit (Alert is displayed when exceeded)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.padding(vertical = 8.dp))
                OutlinedTextField(
                    value = pastWeeksToCheckString,
                    onValueChange = { pastWeeksToCheckString = it },
                    label = { Text("Number of past weeks to check for alerts") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val hours = weeklyHourLimitString.toDoubleOrNull() ?: getDefaultWeeklyThreshold()
                        val weeks = pastWeeksToCheckString.toIntOrNull() ?: getDefaultPastWeeksToCheck()
                        onSaveSettings(hours, weeks)
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    SETTINGS("Settings", R.drawable.ic_settings),
}
