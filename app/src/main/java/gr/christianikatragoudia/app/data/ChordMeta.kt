package gr.christianikatragoudia.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import gr.christianikatragoudia.app.music.MusicNote

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
    val zoom: Int = 0,
)
