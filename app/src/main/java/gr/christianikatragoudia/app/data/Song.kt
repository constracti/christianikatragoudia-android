package gr.christianikatragoudia.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity(tableName = "song")
data class Song(
    @PrimaryKey val id: Int,
    val date: LocalDateTime,
    val content: String,
    val title: String,
    val excerpt: String,
    val modified: LocalDateTime,
    val permalink: String,
)
