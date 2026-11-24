package com.example.fianzapp.domain

import java.time.LocalDate

/*
 * A3 and A7: the rules of the days of warning and of the reminder, with no Android code.
 */

/** The most days of warning accepted: more than a month makes no sense for a one-month deadline. */
const val MAX_WARNING_DAYS = 31

/** "7" → 7. Null for letters, an empty field, 0, a negative number or more than 31. */
fun parseWarningDays(text: String): Int? {
    val days = text.trim().toIntOrNull() ?: return null
    return days.takeIf { it in 1..MAX_WARNING_DAYS }
}

/** The day the warning arrives: [warningDays] days before the deadline. */
fun reminderDay(deadline: LocalDate, warningDays: Int): LocalDate = deadline.minusDays(warningDays.toLong())

enum class Reminder { NONE, WARNING, DEADLINE }

/**
 * What the worker must notify today.
 *
 * - From the reminder day until the day before the deadline: the warning, once per deadline.
 *   If no run happened on the reminder day itself, the next run still sends it.
 * - From the deadline on: the "deadline reached" notification, once per deadline.
 *
 * [warningSentFor] and [deadlineSentFor] are the deadlines already notified (saved in DataStore),
 * so two runs on the same day never send the same reminder twice.
 */
fun reminderToSend(
    deadline: LocalDate,
    today: LocalDate,
    warningDays: Int,
    warningSentFor: String?,
    deadlineSentFor: String?
): Reminder {
    val key = deadline.toString()
    return when {
        !today.isBefore(deadline) ->
            if (deadlineSentFor == key) Reminder.NONE else Reminder.DEADLINE
        !today.isBefore(reminderDay(deadline, warningDays)) ->
            if (warningSentFor == key) Reminder.NONE else Reminder.WARNING
        else -> Reminder.NONE
    }
}
