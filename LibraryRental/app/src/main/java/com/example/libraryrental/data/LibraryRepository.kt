package com.example.libraryrental.data

import androidx.room.withTransaction
import com.example.libraryrental.logic.BookFilter
import com.example.libraryrental.logic.InputValidator
import com.example.libraryrental.logic.RenewResult
import com.example.libraryrental.logic.RentalPolicy
import com.example.libraryrental.logic.ReservationResult
import kotlinx.coroutines.flow.Flow

class LibraryRepository(private val database: LibraryDatabase) {

    private val bookDao = database.bookDao()
    private val bookingDao = database.bookingDao()

    fun getBooks(query: String, filter: BookFilter): Flow<List<Book>> {
        val cleaned = InputValidator.sanitizeSearchQuery(query)
        val pattern = InputValidator.escapeForLike(cleaned)
        val hasQuery = cleaned.isNotEmpty()

        return when (filter) {
            BookFilter.ALL ->
                if (hasQuery) bookDao.searchBooks(pattern) else bookDao.getAllBooks()
            BookFilter.AVAILABLE ->
                if (hasQuery) bookDao.searchBooksByAvailability(pattern, true)
                else bookDao.getBooksByAvailability(true)
            BookFilter.BORROWED ->
                if (hasQuery) bookDao.searchBooksByAvailability(pattern, false)
                else bookDao.getBooksByAvailability(false)
        }
    }

    fun getActiveBookings(): Flow<List<BookingWithBook>> {
        return bookingDao.getActiveBookings()
    }

    fun getReturnedBookings(): Flow<List<BookingWithBook>> {
        return bookingDao.getReturnedBookings()
    }

    suspend fun reserveBook(book: Book, userName: String, rentalDays: Int): ReservationResult {
        val name = InputValidator.sanitizeName(userName)
        if (!InputValidator.isValidName(name)) return ReservationResult.InvalidName
        if (!RentalPolicy.isValidRentalDays(rentalDays)) return ReservationResult.InvalidDuration

        return database.withTransaction<ReservationResult> {
            val current = bookDao.getBookById(book.bookId)
            if (current == null || !current.isAvailable) {
                return@withTransaction ReservationResult.BookUnavailable
            }

            val now = System.currentTimeMillis()
            bookingDao.insertBooking(
                Booking(
                    bookOwnerId = book.bookId,
                    userName = name,
                    bookingDate = now,
                    returnDeadline = RentalPolicy.deadlineFrom(now, rentalDays),
                    status = BookingStatus.PENDING
                )
            )
            bookDao.setAvailability(book.bookId, false)
            ReservationResult.Success
        }
    }

    suspend fun confirmPickup(bookingId: Long): Boolean {
        return bookingDao.activatePending(bookingId) > 0
    }

    suspend fun renewBooking(bookingId: Long, extraDays: Int): RenewResult {
        if (!RentalPolicy.isValidRenewalDays(extraDays)) return RenewResult.InvalidDuration

        return database.withTransaction<RenewResult> {
            val booking = bookingDao.getBookingById(bookingId)
            if (booking == null || booking.status != BookingStatus.ACTIVE) {
                return@withTransaction RenewResult.NotActive
            }

            val start = maxOf(booking.returnDeadline, System.currentTimeMillis())
            val newDeadline = RentalPolicy.deadlineFrom(start, extraDays)
            if (RentalPolicy.exceedsMaxLoan(booking.bookingDate, newDeadline)) {
                return@withTransaction RenewResult.ExceedsMaxLoan
            }

            bookingDao.extendDeadline(bookingId, newDeadline)
            RenewResult.Renewed
        }
    }

    suspend fun returnBook(bookingId: Long, bookId: Long): Boolean {
        return database.withTransaction {
            val returned = bookingDao.markReturned(bookingId) > 0
            if (returned) {
                bookDao.setAvailability(bookId, true)
            }
            returned
        }
    }

    suspend fun cancelBooking(bookingId: Long, bookId: Long): Boolean {
        return database.withTransaction {
            val cancelled = bookingDao.cancelPendingBooking(bookingId) > 0
            if (cancelled) {
                bookDao.setAvailability(bookId, true)
            }
            cancelled
        }
    }
}
