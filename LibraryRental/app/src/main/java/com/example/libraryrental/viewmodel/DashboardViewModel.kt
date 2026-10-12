package com.example.libraryrental.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryrental.data.BookingWithBook
import com.example.libraryrental.data.LibraryRepository
import com.example.libraryrental.logic.RenewResult
import com.example.libraryrental.logic.RentalPolicy
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: LibraryRepository) : ViewModel() {

    val activeBookings: StateFlow<List<BookingWithBook>> = repository.getActiveBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<BookingWithBook>> = repository.getReturnedBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var message by mutableStateOf<String?>(null)
        private set

    fun confirmPickup(bookingId: Long) {
        viewModelScope.launch {
            if (repository.confirmPickup(bookingId)) {
                message = "Pickup confirmed."
            }
        }
    }

    fun renew(bookingId: Long, extraDays: Int) {
        viewModelScope.launch {
            message = when (repository.renewBooking(bookingId, extraDays)) {
                RenewResult.Renewed -> "Renewed for $extraDays more days."
                RenewResult.NotActive -> "Only active rentals can be renewed."
                RenewResult.InvalidDuration -> "Choose a valid renewal period."
                RenewResult.ExceedsMaxLoan ->
                    "A book can't be on loan for more than ${RentalPolicy.MAX_LOAN_DAYS} days in total."
            }
        }
    }

    fun returnBook(bookingId: Long, bookId: Long) {
        viewModelScope.launch {
            if (repository.returnBook(bookingId, bookId)) {
                message = "Book returned."
            }
        }
    }

    fun cancel(bookingId: Long, bookId: Long) {
        viewModelScope.launch {
            if (repository.cancelBooking(bookingId, bookId)) {
                message = "Reservation cancelled."
            }
        }
    }

    fun clearMessage() {
        message = null
    }
}
