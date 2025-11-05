package gr.christianikatragoudia.app.music

import androidx.room.TypeConverter


class MusicNoteTypeConverter {

    @TypeConverter
    fun toNotation(value: MusicNote?): String? {
        return MusicNote.toNotationOrNull(value)
    }

    @TypeConverter
    fun byNotation(string: String?): MusicNote? {
        if (string == null)
            return null
        return MusicNote.byNotation(string)
    }
}
