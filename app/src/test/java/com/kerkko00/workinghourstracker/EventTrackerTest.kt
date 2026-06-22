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

    val testDate1: LocalDate = LocalDate.of(2020, 6, 17) // Wednesday
    val testDate2: LocalDate = LocalDate.of(2020, 6, 18) // Thursday
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
        fun eventDateAlreadyInUse_eventIsOverwritten() {
            eventTracker.addEvent(testDate1, testTrackedHours2)
            assertEquals(1, eventTracker.getEvents().size)
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

    @Nested
    inner class WeeklyCalculations {
        private val monday: LocalDate = LocalDate.of(2024, 6, 17)
        private val tuesday: LocalDate = LocalDate.of(2024, 6, 18)
        private val nextMonday: LocalDate = LocalDate.of(2024, 6, 24)

        @Test
        fun weeklyTotal_correctlySumsHoursForCurrentWeek() {
            eventTracker.addEvent(monday, 8.0)
            eventTracker.addEvent(tuesday, 7.5)
            eventTracker.addEvent(nextMonday, 5.0)

            val total = eventTracker.getWeeklyTotal(monday)
            assertEquals(15.5, total)
        }

        @Test
        fun getExceedingWeeks_correctlyIdentifiesOverThreshold() {
            val threshold = 10.0
            // Week 1: 15.5 (Over)
            eventTracker.addEvent(monday, 8.0)
            eventTracker.addEvent(tuesday, 7.5)
            
            // Week 2: 5.0 (Under)
            eventTracker.addEvent(nextMonday, 5.0)

            val exceeding = eventTracker.getExceedingWeeks(nextMonday, 1, threshold)
            
            assertEquals(1, exceeding.size)
            assertEquals(monday, exceeding[0].first)
            assertEquals(15.5, exceeding[0].second)
        }

        @Test
        fun getExceedingWeeks_includesCurrentWeekIfOver() {
            val threshold = 5.0
            eventTracker.addEvent(monday, 8.0)

            val exceeding = eventTracker.getExceedingWeeks(monday, 1, threshold)
            
            assertEquals(1, exceeding.size)
            assertEquals(monday, exceeding[0].first)
        }
        
        @Test
        fun getPastWeeklyTotals_returnsCorrectDates() {
            val pastTotals = eventTracker.getPastWeeklyTotals(nextMonday, 2)
            assertEquals(2, pastTotals.size)
            assertEquals(monday, pastTotals[0].first)
            assertEquals(LocalDate.of(2024, 6, 10), pastTotals[1].first)
        }
    }
}
