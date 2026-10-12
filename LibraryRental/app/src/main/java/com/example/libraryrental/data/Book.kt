package com.example.libraryrental.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val bookId: Long = 0L,
    val title: String,
    val author: String,
    val category: String,
    val isAvailable: Boolean = true
)
