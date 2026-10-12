package com.example.libraryrental.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class BookingStatus {
    PENDING,
    ACTIVE,
    RETURNED
}

@Entity(
    tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["bookId"],
            childColumns = ["bookOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookOwnerId")]
)
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val bookingId: Long = 0L,
    val bookOwnerId: Long,
    val userName: String,
    val bookingDate: Long,
    val returnDeadline: Long,
    val status: BookingStatus = BookingStatus.PENDING
)
