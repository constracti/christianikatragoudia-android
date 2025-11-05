package gr.christianikatragoudia.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert


@Dao
interface ChordDao {

    @Insert
    suspend fun insert(chord: Chord)

    @Update
    suspend fun update(chord: Chord)

    @Upsert
    suspend fun upsert(chordMeta: ChordMeta)

    @Delete
    suspend fun delete(chord: Chord)

    @Query("SELECT * FROM chord")
    suspend fun getDataList(): List<Chord>

    @Query("SELECT * FROM chord WHERE parent = :parent")
    suspend fun getDataByParent(parent: Int): List<Chord>

    @Query("SELECT * FROM chord_meta WHERE id = :id")
    suspend fun getMetaById(id: Int): ChordMeta?

    @Query("UPDATE chord_meta SET tonality = NULL")
    suspend fun resetTonality()

    @Query("UPDATE chord_meta SET scale = 1")
    suspend fun resetScale()

    @Query("SELECT COUNT(*) FROM chord WHERE speed IS NOT NULL")
    suspend fun countSpeed(): Int
}
