package com.example.fianzapp.domain

import java.time.LocalDate

/**
 * A7: the day from which the part of the deposit that must be returned earns legal interest
 * (art. 36.4 LAU), one month after the tenant hands back the keys.
 *
 * The month is counted from date to date (art. 5 Código Civil). When the end month has no such
 * day, the period ends on the last day of that month, which is what [LocalDate.plusMonths] does.
 * Non-working days are not taken out. Not legal advice.
 */
fun depositDeadline(keysReturned: LocalDate): LocalDate = keysReturned.plusMonths(1)
