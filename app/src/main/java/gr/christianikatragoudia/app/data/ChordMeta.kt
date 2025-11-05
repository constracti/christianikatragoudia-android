package gr.christianikatragoudia.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import gr.christianikatragoudia.app.music.MusicNote
import kotlin.math.pow


@Entity(
    tableName = "chord_meta",
    foreignKeys = [
        ForeignKey(
            entity = Chord::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ChordMeta(
    @PrimaryKey val id: Int,
    val tonality: MusicNote? = null,
    val scale: Float = DEFAULT_SCALE,
    val speed: Float? = null, // TODO reset globally
) {

    companion object {

        const val DEFAULT_SCALE = 1f
        val minScale = 2f.pow(-2)
        val maxScale = 2f.pow(+2)
        val scaleStep = 2f.pow(0.1f)
    }
}
