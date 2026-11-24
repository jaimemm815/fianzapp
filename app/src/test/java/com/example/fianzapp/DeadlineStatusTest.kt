package com.example.fianzapp

import com.example.fianzapp.domain.DeadlineStatus
import com.example.fianzapp.domain.deadlineStatus
import com.example.fianzapp.domain.depositDeadline
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Keys back on 15 March 2027 give a deadline of 15 April 2027 (worked out by hand). */
class DeadlineStatusTest {

    private val deadline = depositDeadline(LocalDate.of(2027, 3, 15))

    @Test
    fun beforeTheDeadline_daysLeft() {
        assertEquals(DeadlineStatus.DaysLeft(12), deadlineStatus(deadline, LocalDate.of(2027, 4, 3)))
    }

    @Test
    fun onTheDeadline_today() {
        assertEquals(DeadlineStatus.Today, deadlineStatus(deadline, LocalDate.of(2027, 4, 15)))
    }

    @Test
    fun afterTheDeadline_passed() {
        assertEquals(DeadlineStatus.Passed(2), deadlineStatus(deadline, LocalDate.of(2027, 4, 17)))
    }
}
