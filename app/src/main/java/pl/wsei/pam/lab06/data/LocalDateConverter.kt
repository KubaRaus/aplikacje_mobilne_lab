package pl.wsei.pam.lab06.data

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LocalDateConverter {
    companion object {
        private const val PATTERN = "yyyy-MM-dd"

        fun fromMillis(millis: Long): LocalDate {
            return Instant
                .ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

        fun toMillis(date: LocalDate): Long {
            return Instant.ofEpochSecond(date.toEpochDay() * 24 * 60 * 60).toEpochMilli()
        }
    }

    @TypeConverter
    fun fromDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern(PATTERN))
    }

    @TypeConverter
    fun toDate(value: String): LocalDate {
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(PATTERN))
    }
}

