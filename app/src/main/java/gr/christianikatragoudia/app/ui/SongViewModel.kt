package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.Chord
import gr.christianikatragoudia.app.data.ChordMeta
import gr.christianikatragoudia.app.data.SettingsRepo
import gr.christianikatragoudia.app.data.Song
import gr.christianikatragoudia.app.data.SongMeta
import gr.christianikatragoudia.app.data.TheDatabase
import gr.christianikatragoudia.app.music.MusicNote
import gr.christianikatragoudia.app.network.BASE_URL
import gr.christianikatragoudia.app.network.TheAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SongViewModel(
    private val songId: Int,
    private val application: TheApplication,
) : ViewModel() {

    private fun analyticsClass(song: Song): String {
        return song.permalink.replace(BASE_URL, "/")
    }

    private fun analyticsName(song: Song): String {
        return song.title + " – " + application.getString(R.string.app_name)
    }

    data class UiState(
        val song: Song? = null,
        val chord: Chord? = null,
        val songMeta: SongMeta? = null,
        val chordMeta: ChordMeta? = null,
        val hiddenTonalities: Set<MusicNote> = setOf(),
        val loading: Boolean = true,
        val passed: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val song = TheDatabase.getInstance(application).songDao().getById(songId)
            val chord = TheDatabase.getInstance(application).chordDao().getByParent(songId).firstOrNull()
            if (song != null && chord != null) {
                val songMeta =
                    (TheDatabase.getInstance(application).songMetaDao().getById(song.id) ?: SongMeta(song.id)).visit()
                TheDatabase.getInstance(application).songMetaDao().upsert(songMeta)
                val chordMeta = TheDatabase.getInstance(application).chordMetaDao().getById(chord.id)
                    ?: ChordMeta(chord.id)
                val hiddenTonalities = SettingsRepo(application).getHiddenTonalities()
                _uiState.update {
                    it.copy(
                        song = song,
                        chord = chord,
                        songMeta = songMeta,
                        chordMeta = chordMeta,
                        hiddenTonalities = hiddenTonalities,
                        loading = false,
                        passed = true,
                    )
                }
                TheAnalytics.logScreenViewWithTonality(
                    analyticsClass(song),
                    analyticsName(song),
                    MusicNote.toNotationOrElse(chordMeta.tonality, MusicNote.NOTATION_NULL),
                )
            } else {
                _uiState.update {
                    it.copy(loading = false)
                }
            }
        }
    }

    fun setStarred(starred: Boolean) {
        val songMeta = _uiState.value.songMeta?.copy(starred = starred) ?: return
        _uiState.update {
            it.copy(songMeta = songMeta)
        }
        viewModelScope.launch {
            TheDatabase.getInstance(application).songMetaDao().upsert(songMeta)
        }
    }

    fun setTonality(tonality: MusicNote?) {
        val song = _uiState.value.song ?: return
        val chordMeta = _uiState.value.chordMeta?.copy(tonality = tonality) ?: return
        _uiState.update {
            it.copy(chordMeta = chordMeta)
        }
        viewModelScope.launch {
            TheDatabase.getInstance(application).chordMetaDao().upsert(chordMeta)
            TheAnalytics.logScreenViewWithTonality(
                analyticsClass(song),
                analyticsName(song),
                MusicNote.toNotationOrElse(chordMeta.tonality, MusicNote.NOTATION_NULL),
            )
        }
    }

    fun setSongZoom(zoom: Int) {
        val songMeta = _uiState.value.songMeta?.copy(zoom = zoom) ?: return
        _uiState.update {
            it.copy(songMeta = songMeta)
        }
        viewModelScope.launch {
            TheDatabase.getInstance(application).songMetaDao().upsert(songMeta)
        }
    }

    fun setChordZoom(zoom: Int) {
        val chordMeta = _uiState.value.chordMeta?.copy(zoom = zoom) ?: return
        _uiState.update {
            it.copy(chordMeta = chordMeta)
        }
        viewModelScope.launch {
            TheDatabase.getInstance(application).chordMetaDao().upsert(chordMeta)
        }
    }
}
