package com.example.libraryrental.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RentalPolicyTest {

    private val day = RentalPolicy.DAY_IN_MILLIS
    private val now = 1_000_000_000_000L

    @Test
    fun rentalDays_onlyOfferedOptionsAreValid() {
        assertTrue(RentalPolicy.isValidRentalDays(7))
        assertTrue(RentalPolicy.isValidRentalDays(30))
        assertFalse(RentalPolicy.isValidRentalDays(0))
        assertFalse(RentalPolicy.isValidRentalDays(-7))
        assertFalse(RentalPolicy.isValidRentalDays(8))
    }

    @Test
    fun renewalDays_onlyOfferedOptionsAreValid() {
        assertTrue(RentalPolicy.isValidRenewalDays(14))
        assertFalse(RentalPolicy.isValidRenewalDays(30))
        assertFalse(RentalPolicy.isValidRenewalDays(0))
    }

    @Test
    fun deadlineFrom_addsWholeDays() {
        assertEquals(now + 7 * day, RentalPolicy.deadlineFrom(now, 7))
    }

    @Test
    fun exceedsMaxLoan_boundary() {
        assertFalse(RentalPolicy.exceedsMaxLoan(now, now + RentalPolicy.MAX_LOAN_DAYS * day))
        assertTrue(RentalPolicy.exceedsMaxLoan(now, now + RentalPolicy.MAX_LOAN_DAYS * day + 1))
    }

    @Test
    fun daysRemaining_roundsUp() {
        assertEquals(7, RentalPolicy.daysRemaining(now + 7 * day, now))
        assertEquals(7, RentalPolicy.daysRemaining(now + 6 * day + day / 2, now))
        assertEquals(1, RentalPolicy.daysRemaining(now + 1, now))
        assertEquals(0, RentalPolicy.daysRemaining(now, now))
    }

    @Test
    fun daysRemaining_overdueIsNegative() {
        assertEquals(-1, RentalPolicy.daysRemaining(now - 1, now))
        assertEquals(-1, RentalPolicy.daysRemaining(now - day, now))
        assertEquals(-2, RentalPolicy.daysRemaining(now - day - 1, now))
    }
}
