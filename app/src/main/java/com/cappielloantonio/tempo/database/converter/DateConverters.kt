package com.cappielloantonio.tempo.database.converter

import androidx.room.TypeConverter
import java.util.*

class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
