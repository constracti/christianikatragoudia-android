package gr.christianikatragoudia.app.data

import android.icu.text.Normalizer2
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey


@Entity(tableName = "song_fts")
@Fts4
data class SongFts (
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    val title: String,
    val content: String,
) {

    constructor(song: Song): this(
        id = song.id,
        title = tokenize(song.title),
        content = tokenize(song.content),
    )

    companion object {

        fun tokenize(string: String): String {
            val lowercase = string
                .replace(Regex("<[^>]*>"), " ")
                .lowercase()
            val normalized = Normalizer2.getNFDInstance().normalize(lowercase)
            return normalized
                .toCharArray().map {
                    when (it) {
                        Char(0x03c2) -> // small final sigma
                            Char(0x03c3) // small sigma
                        else -> it
                    }
                }.filter {
                    it.category != CharCategory.NON_SPACING_MARK
                }.map {
                    when (it.category) {
                        CharCategory.LOWERCASE_LETTER,
                        CharCategory.DECIMAL_DIGIT_NUMBER -> it
                        else -> ' '
                    }
                }.toCharArray().concatToString()
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        fun getColumnWeight(columnIndex: Int): Float {
            return when (columnIndex) {
                0 -> 2f
                else -> 1f
            }
        }
    }
}
