package com.example.libraryrental.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBooks(books: List<Book>)

    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isAvailable = :available ORDER BY title ASC")
    fun getBooksByAvailability(available: Boolean): Flow<List<Book>>

    @Query(
        """
        SELECT * FROM books
        WHERE title LIKE '%' || :query || '%' ESCAPE '\'
           OR author LIKE '%' || :query || '%' ESCAPE '\'
        ORDER BY title ASC
        """
    )
    fun searchBooks(query: String): Flow<List<Book>>

    @Query(
        """
        SELECT * FROM books
        WHERE isAvailable = :available
          AND (title LIKE '%' || :query || '%' ESCAPE '\'
               OR author LIKE '%' || :query || '%' ESCAPE '\')
        ORDER BY title ASC
        """
    )
    fun searchBooksByAvailability(query: String, available: Boolean): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE bookId = :bookId")
    suspend fun getBookById(bookId: Long): Book?

    @Query("UPDATE books SET isAvailable = :isAvailable WHERE bookId = :bookId")
    suspend fun setAvailability(bookId: Long, isAvailable: Boolean)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)
}
