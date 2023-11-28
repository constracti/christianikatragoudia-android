package gr.christianikatragoudia.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ChordMetaDao {

    @Upsert
    suspend fun upsert(chordMeta: ChordMeta)

    @Query("SELECT * FROM chord_meta WHERE id = :id")
    suspend fun getById(id: Int): ChordMeta?

    @Query("UPDATE chord_meta SET tonality = NULL")
    suspend fun resetTonality()

    @Query("UPDATE chord_meta SET zoom = 0")
    suspend fun resetZoom()
}
