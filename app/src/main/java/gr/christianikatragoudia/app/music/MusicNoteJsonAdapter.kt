package gr.christianikatragoudia.app.music

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson

class MusicNoteJsonAdapter {

    @ToJson
    fun toNotation(note: MusicNote): String =
        MusicNote.toNotation(note)

    @FromJson
    fun byNotation(notation: String): MusicNote =
        MusicNote.byNotation(notation) ?: throw JsonDataException()
}
