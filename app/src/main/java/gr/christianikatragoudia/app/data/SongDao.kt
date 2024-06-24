package gr.christianikatragoudia.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert
    suspend fun insert(vararg songs: Song)

    @Update
    suspend fun update(vararg songs: Song)

    @Delete
    suspend fun delete(vararg songs: Song)

    @Query("SELECT * FROM song WHERE id = :id")
    suspend fun getById(id: Int): Song?

    @Query("SELECT * FROM song")
    suspend fun getAll(): List<Song>

    @Query("SELECT COUNT(*) FROM song")
    suspend fun count(): Int

    @Query(
        """
        SELECT DISTINCT song.id, song.title, song.excerpt
        FROM song
        JOIN chord ON chord.parent = song.id
        WHERE
            REPLACE(REPLACE(REPLACE(
                REPLACE(
                    REPLACE(
                        REPLACE(song.title, '-', ' '),
                    '''', ' '),
                ',', ' '),
            ' ', '<>'), '><', ''), '<>', ' ') LIKE :query
            OR
            REPLACE(REPLACE(REPLACE(
                REPLACE(
                    REPLACE(
                        REPLACE(
                            REPLACE(REPLACE(
                                REPLACE(REPLACE(
                                    REPLACE(REPLACE(
                                        REPLACE(song.content, '<hr />', ' '),
                                    '<i>', ' '), '</i>', ' '),
                                '<em>', ' '), '</em>', ' '),
                            CHAR(10), ' '), CHAR(13), ' '),
                        '-', ' '),
                    '''', ' '),
                ',', ' '),
            ' ', '<>'), '><', ''), '<>', ' ') LIKE :query
        ORDER BY song.title COLLATE UNICODE ASC, song.excerpt COLLATE UNICODE ASC, song.id ASC
        """
    )
    fun getTitlesByQuery(query: String): Flow<List<SongTitle>>

    @Query(
        """
        SELECT DISTINCT song.id, song.title, song.excerpt
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
        SELECT DISTINCT song.id, song.title, song.excerpt
        FROM song
        JOIN chord ON chord.parent = song.id
        JOIN song_meta ON song_meta.id = song.id
        WHERE song_meta.visited IS NOT NULL
        ORDER BY song_meta.visited DESC
        """
    )
    fun getTitlesByVisited(): Flow<List<SongTitle>>
}
