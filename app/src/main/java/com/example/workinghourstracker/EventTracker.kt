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
import java.time.temporal.TemporalAdjusters

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

    fun getExceedingWeeks(referenceDate: LocalDate, pastWeeksCount: Int, threshold: Double): List<Pair<LocalDate, Double>> {
        val currentMonday = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val currentTotal = getWeeklyTotal(referenceDate)

        val allWeeks = listOf(currentMonday to currentTotal) + getPastWeeklyTotals(referenceDate, pastWeeksCount)

        return allWeeks.filter { it.second > threshold }
    }

    private fun getPastWeeklyTotals(referenceDate: LocalDate, count: Int): List<Pair<LocalDate, Double>> {
        val currentMonday = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return (1..count).map { weeksBack ->
            val monday = currentMonday.minusWeeks(weeksBack.toLong())
            monday to getWeeklyTotal(monday)
        }
    }

    private fun getWeeklyTotal(referenceDate: LocalDate): Double {
        val monday = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        return events
            .filter { !it.date.isBefore(monday) && !it.date.isAfter(sunday) }
            .sumOf { it.trackedHours }
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
