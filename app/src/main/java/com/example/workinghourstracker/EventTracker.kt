package com.example.workinghourstracker

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate

data class TrackedEvent(
    val date: LocalDate,
    val weekDay: DayOfWeek,
    var trackedHours: Double
)

class LocalDateAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate?) {
        out.value(value?.toString())
    }

    override fun read(`in`: JsonReader): LocalDate? {
        return LocalDate.parse(`in`.nextString())
    }
}

class EventTracker {
    private val events = mutableSetOf<TrackedEvent>()
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    fun addEvent(date: LocalDate, trackedHours: Double) {
        // Remove existing event for the same date if it exists
        events.removeIf { it.date == date }
        events.add(TrackedEvent(date, date.dayOfWeek, trackedHours))
    }

    fun editEvent(date: LocalDate, newTrackedHours: Double) {
        for (evt in events) {
            if (evt.date == date) {
                evt.trackedHours = newTrackedHours
                break
            }
        }
    }

    fun removeEvent(date: LocalDate) {
        events.removeIf { it.date == date }
    }

    fun getEvents(): Set<TrackedEvent> {
        return events
    }

    fun saveToFile(file: File) {
        val json = gson.toJson(events)
        file.writeText(json)
    }

    fun loadFromFile(file: File) {
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<Set<TrackedEvent>>() {}.type
            val loadedEvents: Set<TrackedEvent> = gson.fromJson(json, type)
            events.clear()
            events.addAll(loadedEvents)
        }
    }
}
