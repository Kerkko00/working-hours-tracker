package com.example.workinghourstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.workinghourstracker.ui.theme.WorkingHoursTrackerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val calendar = remember { Calendar() }
    val events = remember { mutableStateListOf<CalendarEvent>() }

    // Helper to refresh events from calendar
    fun refreshEvents() {
        events.clear()
        events.addAll(calendar.getEvents().sortedByDescending { it.date })
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
            AppDestinations.HOME -> HomeScreen(
                events = events,
                onAddEvent = { date, hours ->
                    calendar.addEvent(date, hours)
                    refreshEvents()
                }
            )
            AppDestinations.SETTINGS -> SettingsScreen()
        }
    }
}

@Composable
fun HomeScreen(
    events: List<CalendarEvent>,
    onAddEvent: (LocalDate, Double) -> Unit
) {
    var dateString by remember { mutableStateOf(LocalDate.now().toString()) }
    var hoursString by remember { mutableStateOf("") }

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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hoursString,
                    onValueChange = { hoursString = it },
                    label = { Text("Hours") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val date = LocalDate.parse(dateString)
                        val hours = hoursString.toDoubleOrNull() ?: 0.0
                        onAddEvent(date, hours)
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
                    EventItem(event)
                }
            }
        }
    }
}

@Composable
fun EventItem(event: CalendarEvent) {
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
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun SettingsScreen() {
    var hoursAlertString by remember { mutableStateOf("37.5") }

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

        Row {
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = hoursAlertString,
                    onValueChange = { hoursAlertString = it },
                    label = { Text("Weekly hour limit (Alert is shown when exceeded)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        // TODO: save to settings
                        // val hours = hoursAlertString.toDoubleOrNull() ?: 0.0
                    }
                ) {
                    Spacer(Modifier.width(8.dp))
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
