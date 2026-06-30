package dev.ridill.oar.core.data.db

import androidx.room.TypeConverter
import dev.ridill.oar.core.domain.util.tryOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DateTimeConverter {

    @TypeConverter
    fun fromDateTimeString(value: String?): LocalDateTime? = tryOrNull {
        value?.let {
            // Try parsing as a UTC instant first (new format: "2026-06-30T05:00:00Z"),
            // then fall back to plain local datetime for existing records ("2026-06-30T10:30:00").
            tryOrNull { Instant.parse(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }
                ?: LocalDateTime.parse(it)
        }
    }

    @TypeConverter
    fun toDateTimeString(dateTime: LocalDateTime?): String? = tryOrNull {
        dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toString()
    }

    @TypeConverter
    fun fromDateString(value: String?): LocalDate? = tryOrNull {
        value?.let { LocalDate.parse(value) }
    }

    @TypeConverter
    fun toDateString(date: LocalDate?): String? = tryOrNull {
        date?.toString()
    }
}