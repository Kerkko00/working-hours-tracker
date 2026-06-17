package com.example.workinghourstracker

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate

class CalendarTest {
    lateinit var calendar: Calendar

    val testDate1: LocalDate = LocalDate.of(2020, 6, 17)
    val testDate2: LocalDate = LocalDate.of(2020, 6, 18)
    val testTrackedHours1: Double = 7.5
    val testTrackedHours2: Double = 5.25

    @BeforeEach
    fun setUp() {
        calendar = Calendar()
    }

    @Test
    fun initiallyCalendarEventsIsEmpty() {
        assertTrue(calendar.getEvents().isEmpty())
    }

    @Nested
    inner class AddingEvents {
        @BeforeEach
        fun setUp() {
            calendar.addEvent(testDate1, testTrackedHours1)
        }

        @Test
        fun eventDate_isCorrect() {
            val newEvent = calendar.getEvents().first()
            assertEquals(testDate1, newEvent.date)
        }

        @Test
        fun eventDateAlreadyInUse_eventIsNotAdded() {
            calendar.addEvent(testDate1, testTrackedHours2)
            val firstEvent = calendar.getEvents().first()
            assertEquals(testTrackedHours1, firstEvent.trackedHours)
        }

        @Test
        fun eventTrackedHours_isCorrect() {
            val newEvent = calendar.getEvents().first()
            assertEquals(testTrackedHours1, newEvent.trackedHours)
        }
    }

    @Nested
    inner class RemovingEvents {
        @Test
        fun existingEvent_isRemoved() {
            calendar.addEvent(testDate1, testTrackedHours1)
            calendar.addEvent(testDate2, testTrackedHours2)
            calendar.removeEvent(testDate1)
            assertEquals(1, calendar.getEvents().size)
        }

        @Test
        fun nonExistentEvent_removalDoesNotThrowException() {
            assertDoesNotThrow {
                calendar.removeEvent(testDate1)
            }
        }
    }

    @Nested
    inner class EditingEvents {
        @BeforeEach
        fun setUp() {
            calendar.addEvent(testDate1, testTrackedHours1)
        }

        @Test
        fun existingEvent_isEdited() {
            calendar.editEvent(testDate1, testTrackedHours2)
            val editedEvent = calendar.getEvents().first()
            assertEquals(testTrackedHours2, editedEvent.trackedHours)
        }

        @Test
        fun nonExistentEvent_editingDoesNotThrowException() {
            assertDoesNotThrow {
                calendar.editEvent(testDate2, testTrackedHours2)
            }
        }
    }
}