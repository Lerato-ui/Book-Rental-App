package com.example.libraryrental.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.libraryrental.logic.BookFilter
import com.example.libraryrental.logic.RenewResult
import com.example.libraryrental.logic.RentalPolicy
import com.example.libraryrental.logic.ReservationResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryRepositoryTest {

    private lateinit var db: LibraryDatabase
    private lateinit var repository: LibraryRepository
    private lateinit var book: Book

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LibraryDatabase::class.java).build()
        repository = LibraryRepository(db)

        val id = db.bookDao().insertBook(Book(title = "Test Book", author = "Author", category = "Test"))
        book = db.bookDao().getBookById(id)!!
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun currentBook() = db.bookDao().getBookById(book.bookId)!!
    private suspend fun bookings() = repository.getActiveBookings().first()


    @Test
    fun reserve_marksBookUnavailable_andCreatesPendingBooking() = runBlocking {
        assertEquals(ReservationResult.Success, repository.reserveBook(book, "Sam", 7))

        assertFalse(currentBook().isAvailable)
        val list = bookings()
        assertEquals(1, list.size)
        assertEquals(BookingStatus.PENDING, list[0].booking.status)
        assertEquals("Sam", list[0].booking.userName)
    }

    @Test
    fun reserve_sanitizesName() = runBlocking {
        repository.reserveBook(book, "  Sam    Smith  ", 7)
        assertEquals("Sam Smith", bookings()[0].booking.userName)
    }

    @Test
    fun reserve_blankName_isRejected_andBookStaysAvailable() = runBlocking {
        assertEquals(ReservationResult.InvalidName, repository.reserveBook(book, "   ", 7))
        assertTrue(currentBook().isAvailable)
        assertTrue(bookings().isEmpty())
    }

    @Test
    fun reserve_invalidDuration_isRejected() = runBlocking {
        assertEquals(ReservationResult.InvalidDuration, repository.reserveBook(book, "Sam", 0))
        assertEquals(ReservationResult.InvalidDuration, repository.reserveBook(book, "Sam", -5))
        assertEquals(ReservationResult.InvalidDuration, repository.reserveBook(book, "Sam", 400))
        assertTrue(currentBook().isAvailable)
    }

    @Test
    fun reserve_sameBookTwice_secondIsRejected() = runBlocking {
        assertEquals(ReservationResult.Success, repository.reserveBook(book, "Sam", 7))
        assertEquals(ReservationResult.BookUnavailable, repository.reserveBook(book, "Alex", 7))
        assertEquals(1, bookings().size)
    }

    @Test
    fun reserve_deadlineIsAfterBookingDate() = runBlocking {
        repository.reserveBook(book, "Sam", 14)
        val booking = bookings()[0].booking
        assertEquals(14 * RentalPolicy.DAY_IN_MILLIS, booking.returnDeadline - booking.bookingDate)
    }


    @Test
    fun cancel_pending_freesBook_andRemovesBooking() = runBlocking {
        repository.reserveBook(book, "Sam", 7)
        val booking = bookings()[0].booking

        assertTrue(repository.cancelBooking(booking.bookingId, book.bookId))

        assertTrue(currentBook().isAvailable)
        assertTrue(bookings().isEmpty())
    }

    @Test
    fun cancel_active_doesNothing() = runBlocking {
        repository.reserveBook(book, "Sam", 7)
        val booking = bookings()[0].booking
        repository.confirmPickup(booking.bookingId)

        assertFalse(repository.cancelBooking(booking.bookingId, book.bookId))

        assertFalse(currentBook().isAvailable)
        assertEquals(BookingStatus.ACTIVE, bookings()[0].booking.status)
    }

    @Test
    fun confirmPickup_movesPendingToActive() = runBlocking {
        repository.reserveBook(book, "Sam", 7)
        val booking = bookings()[0].booking

        assertTrue(repository.confirmPickup(booking.bookingId))

        assertEquals(BookingStatus.ACTIVE, bookings()[0].booking.status)
    }


    @Test
    fun renew_active_extendsDeadline() = runBlocking {
        repository.reserveBook(book, "Sam", 7)
        val booking = bookings()[0].booking
        repository.confirmPickup(booking.bookingId)

        assertEquals(RenewResult.Renewed, repository.renewBooking(booking.bookingId, 7))

        assertEquals(
            booking.returnDeadline + 7 * RentalPolicy.DAY_IN_MILLIS,
            bookings()[0].booking.returnDeadline
        )
    }

    @Test
    fun renew_pending_isRejected() = runBlocking {
        repository.reserveBook(book, "Sam", 7)
        val booking = bookings()[0].booking

        assertEquals(RenewResult.NotActive, repository.renewBooking(booking.bookingId, 7))

        assertEquals(booking.returnDeadline, bookings()[0].booking.returnDeadline)
    }

    @Test
    fun renew_invalidDays_isRejected() = runBlocking {
        repository.reserveBook(book, "Sam", 7)
        val booking = bookings()[0].booking
        repository.confirmPickup(booking.bookingId)

        assertEquals(RenewResult.InvalidDuration, repository.renewBooking(booking.bookingId, 0))
        assertEquals(RenewResult.InvalidDuration, repository.renewBooking(booking.bookingId, -3))
        assertEquals(booking.returnDeadline, bookings()[0].booking.returnDeadline)
    }

    @Test
    fun renew_cannotExceedMaximumLoan() = runBlocking {
        repository.reserveBook(book, "Sam", 30)
        val booking = bookings()[0].booking
        repository.confirmPickup(booking.bookingId)

        assertEquals(RenewResult.Renewed, repository.renewBooking(booking.bookingId, 21))
        val afterFirst = bookings()[0].booking.returnDeadline

        assertEquals(RenewResult.ExceedsMaxLoan, repository.renewBooking(booking.bookingId, 21))
        assertEquals(afterFirst, bookings()[0].booking.returnDeadline)
    }


    @Test
    fun return_active_freesBook_andMovesToHistory() = runBlocking {
        repository.reserveBook(book, "Sam", 7)
        val booking = bookings()[0].booking
        repository.confirmPickup(booking.bookingId)
        assertTrue(repository.getReturnedBookings().first().isEmpty())

        assertTrue(repository.returnBook(booking.bookingId, book.bookId))

        assertTrue(currentBook().isAvailable)
        assertTrue(bookings().isEmpty())
        val history = repository.getReturnedBookings().first()
        assertEquals(1, history.size)
        assertEquals(BookingStatus.RETURNED, history[0].booking.status)
    }

    @Test
    fun return_pending_doesNothing() = runBlocking {
        repository.reserveBook(book, "Sam", 7)
        val booking = bookings()[0].booking

        assertFalse(repository.returnBook(booking.bookingId, book.bookId))

        assertFalse(currentBook().isAvailable)
        assertEquals(BookingStatus.PENDING, bookings()[0].booking.status)
    }

    @Test
    fun deletingBook_cascadesToItsBookings() = runBlocking {
        repository.reserveBook(book, "Sam", 7)
        db.bookDao().deleteBook(currentBook())
        assertTrue(bookings().isEmpty())
    }


    @Test
    fun search_matchesTitleAndAuthor_caseInsensitive() = runBlocking {
        db.bookDao().insertBook(Book(title = "Dune", author = "Frank Herbert", category = "Sci-Fi"))

        assertEquals(1, repository.getBooks("dune", BookFilter.ALL).first().size)
        assertEquals(1, repository.getBooks("HERBERT", BookFilter.ALL).first().size)
        assertEquals(2, repository.getBooks("", BookFilter.ALL).first().size)
    }

    @Test
    fun search_wildcardsAreTreatedAsPlainText() = runBlocking {
        assertTrue(repository.getBooks("%", BookFilter.ALL).first().isEmpty())
        assertTrue(repository.getBooks("_", BookFilter.ALL).first().isEmpty())
        assertTrue(repository.getBooks("T%t", BookFilter.ALL).first().isEmpty())
    }

    @Test
    fun filter_splitsAvailableAndBorrowed() = runBlocking {
        db.bookDao().insertBook(Book(title = "Dune", author = "Frank Herbert", category = "Sci-Fi"))
        repository.reserveBook(book, "Sam", 7)

        assertEquals(2, repository.getBooks("", BookFilter.ALL).first().size)
        assertEquals(listOf("Dune"), repository.getBooks("", BookFilter.AVAILABLE).first().map { it.title })
        assertEquals(listOf("Test Book"), repository.getBooks("", BookFilter.BORROWED).first().map { it.title })
    }

    @Test
    fun filter_combinesWithSearch() = runBlocking {
        db.bookDao().insertBook(Book(title = "Test Two", author = "Someone", category = "Test"))
        repository.reserveBook(book, "Sam", 7)

        assertEquals(1, repository.getBooks("test", BookFilter.AVAILABLE).first().size)
        assertEquals(1, repository.getBooks("test", BookFilter.BORROWED).first().size)
        assertEquals(2, repository.getBooks("test", BookFilter.ALL).first().size)
    }
}
