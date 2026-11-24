package com.example.fianzapp.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Where today stands against the deposit deadline. No Android code, so it is tested on the computer. */
sealed interface DeadlineStatus {
    data class DaysLeft(val days: Long) : DeadlineStatus
    data object Today : DeadlineStatus
    data class Passed(val daysAgo: Long) : DeadlineStatus
}

fun deadlineStatus(deadline: LocalDate, today: LocalDate): DeadlineStatus {
    val days = ChronoUnit.DAYS.between(today, deadline)
    return when {
        days > 0 -> DeadlineStatus.DaysLeft(days)
        days == 0L -> DeadlineStatus.Today
        else -> DeadlineStatus.Passed(-days)
    }
}
