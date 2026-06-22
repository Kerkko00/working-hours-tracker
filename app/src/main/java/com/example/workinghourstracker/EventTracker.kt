package com.example.workinghourstracker

import java.time.DayOfWeek
import java.time.LocalDate

data class TrackedEvent(
    val date: LocalDate,
    val weekDay: DayOfWeek,
    var trackedHours: Double
)

class EventTracker {
    private val events = mutableSetOf<TrackedEvent>()

    fun addEvent(date: LocalDate, trackedHours: Double) {
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

    fun getEvents (): Set<TrackedEvent> {
        return events
    }
}