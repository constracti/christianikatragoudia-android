package gr.christianikatragoudia.app.data

import androidx.annotation.StringRes
import com.squareup.moshi.Json
import gr.christianikatragoudia.app.R


data class Patch(
    val timestamp: Int,
    @param:Json(name = "song_id_list")
    val songIdSet: Set<Int>,
    @param:Json(name = "chord_id_list")
    val chordIdSet: Set<Int>,
    @param:Json(name = "song_list")
    val songList: List<Song>,
    @param:Json(name = "chord_list")
    val chordList: List<Chord>,
) {

    enum class Action(@param:StringRes val text: Int) {
        ADD(text = R.string.patch_action_add),
        EDIT(text = R.string.patch_action_edit),
        REMOVE(text = R.string.patch_action_remove),
    }
}
