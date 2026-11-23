package com.example.fianzapp.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

/*
 * A1: the rules of the lease form, with no Android code, so they can be tested on the computer.
 */

enum class LeaseFieldError { REQUIRED, NOT_A_DATE, NOT_AN_AMOUNT, NOT_POSITIVE, KEYS_BEFORE_MOVE_IN }

/** The result of checking the form: one error (or null) per field, and the parsed values. */
data class LeaseCheck(
    val address: LeaseFieldError?,
    val landlordName: LeaseFieldError?,
    val moveInDate: LeaseFieldError?,
    val deposit: LeaseFieldError?,
    val keysReturnedDate: LeaseFieldError?,
    val moveIn: LocalDate?,
    val depositCents: Long?,
    val keysReturned: LocalDate?
) {
    val isValid: Boolean
        get() = listOf(address, landlordName, moveInDate, deposit, keysReturnedDate).all { it == null }
}

/** A date written as 2026-11-01, or null if it is not a real date (2026-02-30 is not). */
fun parseDate(text: String): LocalDate? =
    try {
        LocalDate.parse(text.trim())
    } catch (e: DateTimeParseException) {
        null
    }

private val amountPattern = Regex("""\d+([.,]\d{1,2})?""")

/**
 * An amount in euros as cents: "750" → 75000, "750,50" and "750.50" → 75050.
 * Null for letters, a minus sign, thousands separators or more than two decimals.
 */
fun parseDepositCents(text: String): Long? {
    val trimmed = text.trim()
    if (!amountPattern.matches(trimmed)) return null
    return try {
        BigDecimal(trimmed.replace(',', '.')).movePointRight(2).longValueExact()
    } catch (e: ArithmeticException) {
        null
    }
}

/** Cents back to the text the form shows: 75000 → "750", 75050 → "750,50". */
fun formatCents(cents: Long): String {
    val euros = cents / 100
    val rest = cents % 100
    return if (rest == 0L) "$euros" else "$euros,${rest.toString().padStart(2, '0')}"
}

/** Checks every field of the form. The day of handing back the keys is optional. */
fun checkLease(
    address: String,
    landlordName: String,
    moveInDate: String,
    deposit: String,
    keysReturnedDate: String
): LeaseCheck {
    val moveIn = parseDate(moveInDate)
    val cents = parseDepositCents(deposit)
    val keys = parseDate(keysReturnedDate)
    return LeaseCheck(
        address = if (address.isBlank()) LeaseFieldError.REQUIRED else null,
        landlordName = if (landlordName.isBlank()) LeaseFieldError.REQUIRED else null,
        moveInDate = when {
            moveInDate.isBlank() -> LeaseFieldError.REQUIRED
            moveIn == null -> LeaseFieldError.NOT_A_DATE
            else -> null
        },
        deposit = when {
            deposit.isBlank() -> LeaseFieldError.REQUIRED
            cents == null -> LeaseFieldError.NOT_AN_AMOUNT
            cents <= 0 -> LeaseFieldError.NOT_POSITIVE
            else -> null
        },
        keysReturnedDate = when {
            keysReturnedDate.isBlank() -> null
            keys == null -> LeaseFieldError.NOT_A_DATE
            moveIn != null && keys.isBefore(moveIn) -> LeaseFieldError.KEYS_BEFORE_MOVE_IN
            else -> null
        },
        moveIn = moveIn,
        depositCents = cents,
        keysReturned = keys
    )
}
