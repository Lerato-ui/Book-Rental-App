package com.example.libraryrental

import android.app.Application
import com.example.libraryrental.data.LibraryDatabase
import com.example.libraryrental.data.LibraryRepository

class LibraryApplication : Application() {

    val repository: LibraryRepository by lazy {
        LibraryRepository(LibraryDatabase.getDatabase(this))
    }
}
