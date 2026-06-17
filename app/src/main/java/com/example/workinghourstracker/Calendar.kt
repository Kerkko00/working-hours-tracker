package com.example.workinghourstracker

import java.time.DayOfWeek
import java.time.LocalDate

data class CalendarEvent(
    val date: LocalDate,
    val weekDay: DayOfWeek,
    var trackedHours: Double
)

class Calendar {
    private val events = mutableSetOf<CalendarEvent>()

    fun addEvent(date: LocalDate, trackedHours: Double) {
        events.add(CalendarEvent(date, date.dayOfWeek, trackedHours))
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

    fun getEvents (): Set<CalendarEvent> {
        return events
    }
}