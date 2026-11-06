package com.example.fianzapp

import com.example.fianzapp.domain.depositDeadline
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DepositDeadlineTest {

    @Test
    fun ordinaryDay_sameDayNextMonth() {
        assertEquals(LocalDate.of(2026, 12, 15), depositDeadline(LocalDate.of(2026, 11, 15)))
    }

    @Test
    fun dayMissingInNextMonth_lastDayOfThatMonth() {
        // Java's own example: 2007-03-31 plus one month is 2007-04-30.
        assertEquals(LocalDate.of(2007, 4, 30), depositDeadline(LocalDate.of(2007, 3, 31)))
    }

    @Test
    fun endOfJanuary_endOfFebruary_leapAndNonLeap() {
        assertEquals(LocalDate.of(2027, 2, 28), depositDeadline(LocalDate.of(2027, 1, 31)))
        assertEquals(LocalDate.of(2028, 2, 29), depositDeadline(LocalDate.of(2028, 1, 30)))
    }

    @Test
    fun crossesTheYear() {
        assertEquals(LocalDate.of(2027, 1, 20), depositDeadline(LocalDate.of(2026, 12, 20)))
    }
}
