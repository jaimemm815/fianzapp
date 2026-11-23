package com.example.fianzapp

import com.example.fianzapp.domain.LeaseFieldError
import com.example.fianzapp.domain.checkLease
import com.example.fianzapp.domain.formatCents
import com.example.fianzapp.domain.parseDate
import com.example.fianzapp.domain.parseDepositCents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** A1: the awkward cases of the lease form. Expected values worked out by hand. */
class LeaseValidationTest {

    @Test
    fun deposit_acceptsCommaAndPoint() {
        assertEquals(75000L, parseDepositCents("750"))
        assertEquals(75050L, parseDepositCents("750,50"))
        assertEquals(75050L, parseDepositCents("750.50"))
        assertEquals(75050L, parseDepositCents("750,5"))
    }

    @Test
    fun deposit_rejectsLettersNegativeAndThousands() {
        assertNull(parseDepositCents("abc"))
        assertNull(parseDepositCents("-750"))
        assertNull(parseDepositCents("1.000,50"))
        assertNull(parseDepositCents("750,505"))
    }

    @Test
    fun deposit_zeroIsNotPositive() {
        val check = checkLease("Calle Falsa 123", "Invented Landlord", "2026-11-01", "0", "")
        assertEquals(LeaseFieldError.NOT_POSITIVE, check.deposit)
        assertFalse(check.isValid)
    }

    @Test
    fun date_notARealDate() {
        assertNull(parseDate("2026-02-30"))
        assertNull(parseDate("01/11/2026"))
        assertEquals(LocalDate.of(2026, 11, 1), parseDate("2026-11-01"))
    }

    @Test
    fun keysBeforeMoveIn_isAnError() {
        val check = checkLease("Calle Falsa 123", "Invented Landlord", "2026-11-01", "750", "2026-10-31")
        assertEquals(LeaseFieldError.KEYS_BEFORE_MOVE_IN, check.keysReturnedDate)
    }

    @Test
    fun emptyForm_isNotValid_butKeysAreOptional() {
        val empty = checkLease("", "", "", "", "")
        assertEquals(LeaseFieldError.REQUIRED, empty.address)
        assertNull(empty.keysReturnedDate)
        assertFalse(empty.isValid)

        val filled = checkLease("Calle Falsa 123", "Invented Landlord", "2026-11-01", "750,50", "")
        assertTrue(filled.isValid)
        assertEquals(75050L, filled.depositCents)
    }

    @Test
    fun formatCents_backToText() {
        assertEquals("750", formatCents(75000))
        assertEquals("750,50", formatCents(75050))
        assertEquals("750,05", formatCents(75005))
    }
}
