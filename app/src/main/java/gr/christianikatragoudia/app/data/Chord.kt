package gr.christianikatragoudia.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import gr.christianikatragoudia.app.music.MusicNote
import java.time.LocalDateTime

@Entity(
    tableName = "chord",
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["parent"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class Chord(
    @PrimaryKey val id: Int,
    val date: LocalDateTime,
    val modified: LocalDateTime,
    val parent: Int,
    val content: String,
    val tonality: MusicNote,
) {

    fun isSynchronized(other: Chord): Boolean {
        return this.id == other.id && this.modified == other.modified && this.parent == other.parent
    }
}
