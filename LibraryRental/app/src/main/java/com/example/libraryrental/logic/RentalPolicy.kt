package com.example.libraryrental.logic

object RentalPolicy {

    const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
    const val MAX_LOAN_DAYS = 60

    val RENTAL_DAY_OPTIONS = listOf(7, 14, 21, 30)
    val RENEWAL_DAY_OPTIONS = listOf(7, 14, 21)

    fun isValidRentalDays(days: Int): Boolean {
        return days in RENTAL_DAY_OPTIONS
    }

    fun isValidRenewalDays(days: Int): Boolean {
        return days in RENEWAL_DAY_OPTIONS
    }

    fun deadlineFrom(startMillis: Long, days: Int): Long {
        return startMillis + days * DAY_IN_MILLIS
    }

    fun exceedsMaxLoan(bookingDate: Long, deadline: Long): Boolean {
        return deadline - bookingDate > MAX_LOAN_DAYS * DAY_IN_MILLIS
    }

    fun daysRemaining(deadline: Long, now: Long): Int {
        val diff = deadline - now
        return if (diff >= 0) {
            ((diff + DAY_IN_MILLIS - 1) / DAY_IN_MILLIS).toInt()
        } else {
            -(((-diff + DAY_IN_MILLIS - 1) / DAY_IN_MILLIS).toInt())
        }
    }
}
