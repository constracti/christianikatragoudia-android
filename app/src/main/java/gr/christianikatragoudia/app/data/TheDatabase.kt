package gr.christianikatragoudia.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import gr.christianikatragoudia.app.music.MusicNoteTypeConverter

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // chord parent index
        db.execSQL("CREATE INDEX `index_chord_parent` ON `chord` (`parent`)")
        // song meta zoom
        db.execSQL("""
            CREATE TABLE `song_temp` (
                `id` INTEGER NOT NULL,
                `zoom` REAL NOT NULL,
                `starred` INTEGER NOT NULL,
                `visited` TEXT,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`id`) REFERENCES `song`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            INSERT INTO `song_temp` (`id`, `zoom`, `starred`, `visited`)
            SELECT `id`, 1, `starred`, `visited`
            FROM `song_meta`
        """.trimIndent())
        db.execSQL("DROP TABLE `song_meta`")
        db.execSQL("ALTER TABLE `song_temp` RENAME TO `song_meta`")
        // chord meta zoom
        db.execSQL("""
            CREATE TABLE `chord_temp` (
                `id` INTEGER NOT NULL,
                `tonality` TEXT,
                `zoom` REAL NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`id`) REFERENCES `chord`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            INSERT INTO `chord_temp` (`id`, `tonality`, `zoom`)
            SELECT `id`, `tonality`, 1
            FROM `chord_meta`
        """.trimIndent())
        db.execSQL("DROP TABLE `chord_meta`")
        db.execSQL("ALTER TABLE `chord_temp` RENAME TO `chord_meta`")
    }
}

@Database(
    version = 2,
    entities = [
        Song::class,
        Chord::class,
        SongMeta::class,
        ChordMeta::class,
    ],
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

        fun getInstance(context: Context): TheDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    TheDatabase::class.java,
                    "db_main",
                ).addMigrations(
                    MIGRATION_1_2,
                ).fallbackToDestructiveMigration().build().also {
                    Instance = it
                }
            }
        }
    }
}
