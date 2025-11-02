package gr.christianikatragoudia.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.pow


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
    val zoom: Float = DEFAULT_SCALE,
    val starred: Boolean = false,
    val visited: LocalDateTime? = null,
) {

    companion object {

        const val DEFAULT_SCALE = 1f
        val minScale = 2f.pow(-2)
        val maxScale = 2f.pow(+2)
        val scaleStep = 2f.pow(0.1f)
    }

    fun visit(): SongMeta {
        val zoned = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"))
        return copy(visited = zoned.toLocalDateTime())
    }
}
