package com.example.libraryrental.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.libraryrental.data.Book
import com.example.libraryrental.data.LibraryRepository
import com.example.libraryrental.logic.BookFilter
import com.example.libraryrental.logic.InputValidator
import com.example.libraryrental.logic.ReservationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel(private val repository: LibraryRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filter = MutableStateFlow(BookFilter.ALL)
    val filter: StateFlow<BookFilter> = _filter

    val books: StateFlow<List<Book>?> = combine(_searchQuery, _filter) { query, selectedFilter ->
        Pair(query, selectedFilter)
    }.flatMapLatest { (query, selectedFilter) ->
        repository.getBooks(query, selectedFilter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    var selectedBook by mutableStateOf<Book?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    fun onSearchChange(query: String) {
        _searchQuery.value = query.take(InputValidator.MAX_QUERY_LENGTH)
    }

    fun onFilterChange(newFilter: BookFilter) {
        _filter.value = newFilter
    }

    fun selectBook(book: Book) {
        if (book.isAvailable) {
            selectedBook = book
        }
    }

    fun closeSheet() {
        selectedBook = null
    }

    fun reserve(userName: String, rentalDays: Int) {
        val book = selectedBook ?: return
        viewModelScope.launch {
            val result = repository.reserveBook(book, userName, rentalDays)
            message = when (result) {
                ReservationResult.Success -> "\"${book.title}\" reserved. Collect it from the desk."
                ReservationResult.BookUnavailable -> "Sorry, \"${book.title}\" has just been taken."
                ReservationResult.InvalidName ->
                    "Enter a name between ${InputValidator.MIN_NAME_LENGTH} and ${InputValidator.MAX_NAME_LENGTH} characters."
                ReservationResult.InvalidDuration -> "Choose a valid rental period."
            }
            selectedBook = null
        }
    }

    fun clearMessage() {
        message = null
    }
}
