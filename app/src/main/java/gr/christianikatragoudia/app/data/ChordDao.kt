package gr.christianikatragoudia.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChordDao {

    @Insert
    suspend fun insert(vararg chords: Chord)

    @Update
    suspend fun update(vararg chords: Chord)

    @Delete
    suspend fun delete(vararg chord: Chord)

    @Query("SELECT * FROM chord")
    suspend fun getAll(): List<Chord>

    @Query("SELECT * FROM chord WHERE parent = :parent")
    suspend fun getByParent(parent: Int): List<Chord>
}
