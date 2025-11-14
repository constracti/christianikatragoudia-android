package gr.christianikatragoudia.app.data

import android.content.Context
import android.os.Build
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import gr.christianikatragoudia.app.music.MusicNoteTypeConverter


private val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // begin transaction
        db.beginTransaction()
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
        // commit transaction
        db.endTransaction()
    }
}


private val MIGRATION_3_4 = object : Migration(3, 4) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // begin transaction
        db.beginTransaction()
        // song meta scale
        if (Build.VERSION.SDK_INT >= 30) {
            db.execSQL("ALTER TABLE `song_meta` RENAME `zoom` TO `scale`")
        } else {
            db.execSQL("""
                CREATE TABLE `song_temp` (
                    `id` INTEGER NOT NULL,
                    `scale` REAL NOT NULL,
                    `starred` INTEGER NOT NULL,
                    `visited` TEXT,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`id`) REFERENCES `song`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("""
                INSERT INTO `song_temp` (`id`, `scale`, `starred`, `visited`)
                SELECT `id`, `zoom`, `starred`, `visited`
                FROM `song_meta`
            """.trimIndent())
            db.execSQL("DROP TABLE `song_meta`")
            db.execSQL("ALTER TABLE `song_temp` RENAME TO `song_meta`")
        }
        // chord meta scale
        if (Build.VERSION.SDK_INT >= 30) {
            db.execSQL("ALTER TABLE `chord_meta` RENAME `zoom` TO `scale`")
        } else {
            db.execSQL("""
                CREATE TABLE `chord_temp` (
                    `id` INTEGER NOT NULL,
                    `tonality` TEXT,
                    `scale` REAL NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`id`) REFERENCES `chord`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("""
                INSERT INTO `chord_temp` (`id`, `tonality`, `scale`)
                SELECT `id`, `tonality`, `zoom`
                FROM `chord_meta`
            """.trimIndent())
            db.execSQL("DROP TABLE `chord_meta`")
            db.execSQL("ALTER TABLE `chord_temp` RENAME TO `chord_meta`")
        }
        // chord speed
        db.execSQL("ALTER TABLE `chord` ADD `speed` REAL")
        // chord meta speed
        db.execSQL("ALTER TABLE `chord_meta` ADD `speed` REAL")
        // replace final sigma
        val cursor = db.query("SELECT `id`, `title`, `content` FROM `song`")
        val songFtsList = mutableListOf<SongFts>()
        cursor.moveToFirst()
        while (cursor.isAfterLast.not()) {
            val songFts = SongFts(
                id = cursor.getInt(0),
                title = SongFts.tokenize(cursor.getString(1)),
                content = SongFts.tokenize(cursor.getString(2)),
            )
            songFtsList.add(songFts)
            cursor.moveToNext()
        }
        db.execSQL("DELETE FROM `song_fts`")
        val statement = db.compileStatement("INSERT INTO `song_fts` (`rowid`, `title`, `content`) VALUES (?, ?, ?)")
        songFtsList.forEach { songFts ->
            statement.bindLong(1, songFts.id.toLong())
            statement.bindString(2, songFts.title)
            statement.bindString(3, songFts.content)
            statement.executeInsert()
            statement.clearBindings()
        }
        db.execSQL("INSERT INTO `song_fts`(`song_fts`) VALUES ('optimize')")
        // commit transaction
        db.endTransaction()
    }
}


@Database(
    version = 4,
    entities = [
        Song::class,
        Chord::class,
        SongMeta::class,
        ChordMeta::class,
        SongFts::class,
    ],
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3), // song fts
    ],
)
@TypeConverters(
    DateTimeConverter::class,
    MusicNoteTypeConverter::class,
)
abstract class TheDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    abstract fun chordDao(): ChordDao

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
                    MIGRATION_3_4,
                ).fallbackToDestructiveMigration(true).build().also {
                    Instance = it
                }
            }
        }
    }
}
