package gr.christianikatragoudia.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity(
    tableName = "song_meta",
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SongMeta(
    @PrimaryKey val id: Int,
    val zoom: Int = 0,
    val starred: Boolean = false,
    val visited: LocalDateTime? = null,
) {

    fun visit(): SongMeta {
        val zoned = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"))
        return copy(visited = zoned.toLocalDateTime())
    }
}
