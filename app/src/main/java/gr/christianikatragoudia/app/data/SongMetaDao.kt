package gr.christianikatragoudia.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SongMetaDao {

    @Upsert
    suspend fun upsert(songMeta: SongMeta)

    @Query("SELECT * FROM song_meta WHERE id = :id")
    suspend fun getById(id: Int): SongMeta?

    @Query("UPDATE song_meta SET visited = NULL")
    suspend fun clearRecent()

    @Query("UPDATE song_meta SET zoom = 0")
    suspend fun resetZoom()
}
