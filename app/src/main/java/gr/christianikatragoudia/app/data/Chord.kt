package gr.christianikatragoudia.app.data

import androidx.room.ColumnInfo
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
    @ColumnInfo(index = true) val parent: Int,
    val content: String,
    val tonality: MusicNote,
    val speed: Float?,
)
