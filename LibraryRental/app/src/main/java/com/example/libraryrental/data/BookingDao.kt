package com.example.libraryrental.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Insert
    suspend fun insertBooking(booking: Booking): Long

    @Query("SELECT * FROM bookings WHERE bookingId = :bookingId")
    suspend fun getBookingById(bookingId: Long): Booking?

    @Query(
        """
        SELECT
            bookings.bookingId       AS bookingId,
            bookings.bookOwnerId     AS bookOwnerId,
            bookings.userName        AS userName,
            bookings.bookingDate     AS bookingDate,
            bookings.returnDeadline  AS returnDeadline,
            bookings.status          AS status,
            books.bookId             AS book_bookId,
            books.title              AS book_title,
            books.author             AS book_author,
            books.category           AS book_category,
            books.isAvailable        AS book_isAvailable
        FROM bookings
        INNER JOIN books ON bookings.bookOwnerId = books.bookId
        WHERE bookings.status IN ('PENDING', 'ACTIVE')
        ORDER BY bookings.returnDeadline ASC
        """
    )
    fun getActiveBookings(): Flow<List<BookingWithBook>>

    @Query(
        """
        SELECT
            bookings.bookingId       AS bookingId,
            bookings.bookOwnerId     AS bookOwnerId,
            bookings.userName        AS userName,
            bookings.bookingDate     AS bookingDate,
            bookings.returnDeadline  AS returnDeadline,
            bookings.status          AS status,
            books.bookId             AS book_bookId,
            books.title              AS book_title,
            books.author             AS book_author,
            books.category           AS book_category,
            books.isAvailable        AS book_isAvailable
        FROM bookings
        INNER JOIN books ON bookings.bookOwnerId = books.bookId
        WHERE bookings.status = 'RETURNED'
        ORDER BY bookings.bookingId DESC
        """
    )
    fun getReturnedBookings(): Flow<List<BookingWithBook>>

    @Query("UPDATE bookings SET returnDeadline = :newDeadline WHERE bookingId = :bookingId AND status = 'ACTIVE'")
    suspend fun extendDeadline(bookingId: Long, newDeadline: Long): Int

    @Query("UPDATE bookings SET status = 'ACTIVE' WHERE bookingId = :bookingId AND status = 'PENDING'")
    suspend fun activatePending(bookingId: Long): Int

    @Query("UPDATE bookings SET status = 'RETURNED' WHERE bookingId = :bookingId AND status = 'ACTIVE'")
    suspend fun markReturned(bookingId: Long): Int

    @Query("DELETE FROM bookings WHERE bookingId = :bookingId AND status = 'PENDING'")
    suspend fun cancelPendingBooking(bookingId: Long): Int

    @Delete
    suspend fun deleteBooking(booking: Booking)
}
