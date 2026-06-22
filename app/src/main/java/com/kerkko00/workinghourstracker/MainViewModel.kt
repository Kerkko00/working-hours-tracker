package com.kerkko00.workinghourstracker

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import java.io.File
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataFile = File(application.applicationContext.filesDir, "events.json")
    private val settingsFile = File(application.applicationContext.filesDir, "settings.json")
    private val eventTracker = EventTracker()
    private val gson = Gson()

    var events = mutableStateListOf<TrackedEvent>()
        private set

    var weeklyThreshold by mutableDoubleStateOf(getDefaultWeeklyThreshold())
        private set

    var pastWeeksToCheck by mutableIntStateOf(getDefaultPastWeeksToCheck())
        private set

    var exceedingWeeks by mutableStateOf<List<Pair<LocalDate, Double>>>(emptyList())
        private set

    init {
        loadData()
    }

    private fun loadData() {
        // Load events
        eventTracker.loadFromFile(dataFile)
        refreshEvents()

        // Load settings
        if (settingsFile.exists()) {
            val content = settingsFile.readText()
            var isSettingsFileCorrupted = false
            try {
                val settings = gson.fromJson(content, AppSettings::class.java)
                weeklyThreshold = settings.weeklyThreshold
                pastWeeksToCheck = settings.pastWeeksToCheck
            } catch (_: Exception) {
                // Use default values and delete corrupted settings file
                weeklyThreshold = getDefaultWeeklyThreshold()
                pastWeeksToCheck = getDefaultPastWeeksToCheck()
                isSettingsFileCorrupted = true
            }

            if (isSettingsFileCorrupted) { // Try deleting the corrupted settings file
                try {
                    settingsFile.delete()
                } catch (_: Exception) {}
            }
        }
        updateExceedingWeeks()
    }

    private fun refreshEvents() {
        events.clear()
        events.addAll(eventTracker.getEvents().sortedByDescending { it.date })
    }

    private fun updateExceedingWeeks() {
        exceedingWeeks = eventTracker.getExceedingWeeks(LocalDate.now(), pastWeeksToCheck, weeklyThreshold)
    }

    private fun saveEvents() {
        eventTracker.saveToFile(dataFile)
    }

    private fun saveSettings() {
        val settings = AppSettings(weeklyThreshold, pastWeeksToCheck)
        settingsFile.writeText(gson.toJson(settings))
    }

    fun addEvent(date: LocalDate, hours: Double) {
        eventTracker.addEvent(date, hours)
        refreshEvents()
        saveEvents()
        updateExceedingWeeks()
    }

    fun removeEvent(date: LocalDate) {
        eventTracker.removeEvent(date)
        refreshEvents()
        saveEvents()
        updateExceedingWeeks()
    }

    fun editEvent(date: LocalDate, hours: Double) {
        eventTracker.editEvent(date, hours)
        refreshEvents()
        saveEvents()
        updateExceedingWeeks()
    }

    fun updateSettings(newThreshold: Double, newPastWeeks: Int) {
        weeklyThreshold = newThreshold
        pastWeeksToCheck = newPastWeeks
        saveSettings()
        updateExceedingWeeks()
    }
}
