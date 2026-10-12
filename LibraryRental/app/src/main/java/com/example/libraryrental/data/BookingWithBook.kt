package com.example.libraryrental.data

import androidx.room.Embedded

data class BookingWithBook(
    @Embedded
    val booking: Booking,
    @Embedded(prefix = "book_")
    val book: Book
)
