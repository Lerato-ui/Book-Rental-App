package com.example.libraryrental.data

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromStatus(status: BookingStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): BookingStatus {
        return BookingStatus.valueOf(value)
    }
}
