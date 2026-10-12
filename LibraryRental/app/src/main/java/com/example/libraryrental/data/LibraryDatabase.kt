package com.example.libraryrental.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Book::class, Booking::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LibraryDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile
        private var INSTANCE: LibraryDatabase? = null

        fun getDatabase(context: Context): LibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.bookDao()?.insertBooks(sampleBooks())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun sampleBooks(): List<Book> {
            return listOf(
                Book(title = "Clean Code", author = "Robert C. Martin", category = "Software Engineering"),
                Book(title = "The Pragmatic Programmer", author = "Andrew Hunt", category = "Software Engineering"),
                Book(title = "Kotlin in Action", author = "Dmitry Jemerov", category = "Programming"),
                Book(title = "Effective Java", author = "Joshua Bloch", category = "Programming"),
                Book(title = "Dune", author = "Frank Herbert", category = "Science Fiction"),
                Book(title = "1984", author = "George Orwell", category = "Fiction"),
                Book(title = "Sapiens", author = "Yuval Noah Harari", category = "Non-Fiction"),
                Book(title = "Atomic Habits", author = "James Clear", category = "Self-Help"),
                Book(title = "The Hobbit", author = "J.R.R. Tolkien", category = "Fantasy"),
                Book(title = "Deep Work", author = "Cal Newport", category = "Self-Help")
            )
        }
    }
}
