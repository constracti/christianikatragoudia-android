package gr.christianikatragoudia.app.data

import androidx.room.TypeConverter
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeConverter {

    @ToJson
    fun toJson(value: LocalDateTime): String {
        return toStr(value)
    }

    @FromJson
    fun byJson(string: String): LocalDateTime {
        return byStr(string)
    }

    @TypeConverter
    fun toSql(value: LocalDateTime?): String? {
        if (value == null)
            return null
        return toStr(value)
    }

    @TypeConverter
    fun bySql(string: String?): LocalDateTime? {
        if (string == null)
            return null
        return byStr(string)
    }

    companion object {

        private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        fun toStr(value: LocalDateTime): String {
            return value.format(FORMATTER)
        }

        fun byStr(string: String): LocalDateTime {
            return LocalDateTime.parse(string, FORMATTER)
        }
    }
}
