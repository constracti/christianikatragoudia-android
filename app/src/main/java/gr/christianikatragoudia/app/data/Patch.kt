package gr.christianikatragoudia.app.data

import com.squareup.moshi.Json

data class Patch(
    val timestamp: Int,
    @Json(name = "song_id_list")
    val songIdSet: Set<Int>,
    @Json(name = "chord_id_list")
    val chordIdSet: Set<Int>,
    @Json(name = "song_list")
    val songList: List<Song>,
    @Json(name = "chord_list")
    val chordList: List<Chord>,
)
