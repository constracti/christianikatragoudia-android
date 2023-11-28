package gr.christianikatragoudia.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import gr.christianikatragoudia.app.music.MusicNoteTypeConverter

@Database(
    entities = [
        Song::class,
        Chord::class,
        SongMeta::class,
        ChordMeta::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    DateTimeConverter::class,
    MusicNoteTypeConverter::class,
)
abstract class TheDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    abstract fun chordDao(): ChordDao

    abstract fun songMetaDao(): SongMetaDao

    abstract fun chordMetaDao(): ChordMetaDao

    companion object {

        @Volatile
        private var Instance: TheDatabase? = null

        fun getDatabase(context: Context): TheDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    TheDatabase::class.java,
                    "db_main",
                ).fallbackToDestructiveMigration().build().also {
                    Instance = it
                }
            }
        }
    }
}
