package gr.christianikatragoudia.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert
    suspend fun insert(song: Song)

    @Insert
    suspend fun insert(songFts: SongFts)

    @Update
    suspend fun update(song: Song)

    @Update
    suspend fun update(songFts: SongFts)

    @Upsert
    suspend fun upsert(songMeta: SongMeta)

    @Delete
    suspend fun delete(song: Song)

    @Delete
    suspend fun delete(songFts: SongFts)

    @Query("INSERT INTO song_fts(song_fts) VALUES('optimize')")
    suspend fun optimize()

    @Query("SELECT * FROM song WHERE id = :id")
    suspend fun getDataById(id: Int): Song?

    @Query("SELECT * FROM song")
    suspend fun getDataList(): List<Song>

    @Query("SELECT * FROM song_meta WHERE id = :id")
    suspend fun getMetaById(id: Int): SongMeta?

    @Query("SELECT COUNT(*) FROM song")
    suspend fun countData(): Int

    @Query("SELECT COUNT (*) FROM song_fts")
    suspend fun countFts(): Int

    @Query("UPDATE song_meta SET visited = NULL")
    suspend fun clearRecent()

    @Query("UPDATE song_meta SET zoom = 1")
    suspend fun resetZoom()

    @Query(
        """
        SELECT song.id, song.title, song.excerpt
        FROM song
        JOIN chord ON chord.parent = song.id
        ORDER BY song.title COLLATE UNICODE ASC, song.excerpt COLLATE UNICODE ASC, song.id ASC
        """
    )
    fun getTitles(): Flow<List<SongTitle>>

    @Query(
        """
        SELECT song.id, song.title, song.excerpt, matchinfo(song_fts) as matchInfo
        FROM song_fts
        JOIN song ON song.id = song_fts.rowid
        JOIN chord ON chord.parent = song.id
        WHERE song_fts MATCH :query
        ORDER BY song.title COLLATE UNICODE ASC, song.excerpt COLLATE UNICODE ASC, song.id ASC
        """
    )
    fun getMatchesByQuery(query: String): Flow<List<SongMatch>>

    @Query(
        """
        SELECT song.id, song.title, song.excerpt
        FROM song
        JOIN chord ON chord.parent = song.id
        JOIN song_meta ON song_meta.id = song.id
        WHERE song_meta.starred
        ORDER BY song.title COLLATE UNICODE ASC, song.excerpt COLLATE UNICODE ASC, song.id ASC
        """
    )
    fun getTitlesByStarred(): Flow<List<SongTitle>>

    @Query(
        """
        SELECT song.id, song.title, song.excerpt
        FROM song
        JOIN chord ON chord.parent = song.id
        JOIN song_meta ON song_meta.id = song.id
        WHERE song_meta.visited IS NOT NULL
        ORDER BY song_meta.visited DESC
        """
    )
    fun getTitlesByVisited(): Flow<List<SongTitle>>
}
