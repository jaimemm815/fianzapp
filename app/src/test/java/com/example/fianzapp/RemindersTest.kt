package com.example.fianzapp

import com.example.fianzapp.domain.Reminder
import com.example.fianzapp.domain.parseWarningDays
import com.example.fianzapp.domain.reminderDay
import com.example.fianzapp.domain.reminderToSend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** A3 and A7. Deadline 15 April 2027, 7 days of warning → reminder on 8 April (by hand). */
class RemindersTest {

    private val deadline = LocalDate.of(2027, 4, 15)

    @Test
    fun warningDays_validAndInvalid() {
        assertEquals(7, parseWarningDays("7"))
        assertEquals(31, parseWarningDays(" 31 "))
        assertNull(parseWarningDays(""))
        assertNull(parseWarningDays("abc"))
        assertNull(parseWarningDays("-3"))
        assertNull(parseWarningDays("0"))
        assertNull(parseWarningDays("40"))
    }

    @Test
    fun reminderDay_isDaysBeforeTheDeadline() {
        assertEquals(LocalDate.of(2027, 4, 8), reminderDay(deadline, 7))
    }

    @Test
    fun beforeTheReminderDay_nothing() {
        assertEquals(Reminder.NONE, reminderToSend(deadline, LocalDate.of(2027, 4, 7), 7, null, null))
    }

    @Test
    fun onTheReminderDay_warningOnce() {
        val day = LocalDate.of(2027, 4, 8)
        assertEquals(Reminder.WARNING, reminderToSend(deadline, day, 7, null, null))
        assertEquals(Reminder.NONE, reminderToSend(deadline, day, 7, "2027-04-15", null))
    }

    @Test
    fun missedReminderDay_stillWarnsLater() {
        assertEquals(Reminder.WARNING, reminderToSend(deadline, LocalDate.of(2027, 4, 11), 7, null, null))
    }

    @Test
    fun onAndAfterTheDeadline_deadlineOnce() {
        assertEquals(Reminder.DEADLINE, reminderToSend(deadline, deadline, 7, "2027-04-15", null))
        assertEquals(Reminder.NONE, reminderToSend(deadline, deadline.plusDays(1), 7, "2027-04-15", "2027-04-15"))
    }
}
