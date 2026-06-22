package com.kerkko00.workinghourstracker

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate

class EventTrackerTest {
    lateinit var eventTracker: EventTracker

    val testDate1: LocalDate = LocalDate.of(2020, 6, 17)
    val testDate2: LocalDate = LocalDate.of(2020, 6, 18)
    val testTrackedHours1: Double = 7.5
    val testTrackedHours2: Double = 5.25

    @BeforeEach
    fun setUp() {
        eventTracker = EventTracker()
    }

    @Test
    fun initiallyTrackerEventsIsEmpty() {
        assertTrue(eventTracker.getEvents().isEmpty())
    }

    @Nested
    inner class AddingEvents {
        @BeforeEach
        fun setUp() {
            eventTracker.addEvent(testDate1, testTrackedHours1)
        }

        @Test
        fun eventDate_isCorrect() {
            val newEvent = eventTracker.getEvents().first()
            assertEquals(testDate1, newEvent.date)
        }

        @Test
        fun eventDateAlreadyInUse_eventIsNotAdded() {
            eventTracker.addEvent(testDate1, testTrackedHours2)
            val firstEvent = eventTracker.getEvents().first()
            assertEquals(testTrackedHours2, firstEvent.trackedHours)
        }

        @Test
        fun eventTrackedHours_isCorrect() {
            val newEvent = eventTracker.getEvents().first()
            assertEquals(testTrackedHours1, newEvent.trackedHours)
        }
    }

    @Nested
    inner class RemovingEvents {
        @Test
        fun existingEvent_isRemoved() {
            eventTracker.addEvent(testDate1, testTrackedHours1)
            eventTracker.addEvent(testDate2, testTrackedHours2)
            eventTracker.removeEvent(testDate1)
            assertEquals(1, eventTracker.getEvents().size)
        }

        @Test
        fun nonExistentEvent_removalDoesNotThrowException() {
            assertDoesNotThrow {
                eventTracker.removeEvent(testDate1)
            }
        }
    }

    @Nested
    inner class EditingEvents {
        @BeforeEach
        fun setUp() {
            eventTracker.addEvent(testDate1, testTrackedHours1)
        }

        @Test
        fun existingEvent_isEdited() {
            eventTracker.editEvent(testDate1, testTrackedHours2)
            val editedEvent = eventTracker.getEvents().first()
            assertEquals(testTrackedHours2, editedEvent.trackedHours)
        }

        @Test
        fun nonExistentEvent_editingDoesNotThrowException() {
            assertDoesNotThrow {
                eventTracker.editEvent(testDate2, testTrackedHours2)
            }
        }
    }
}