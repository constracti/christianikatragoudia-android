package gr.christianikatragoudia.app.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    companion object {

        val speedList = listOf(
            15f, 15.75f, 16.5f, 17.25f,
            18f, 10f, 20f, 21f,
            22f,  23f, 24f, 25f,
            26f, 27f, 28f, 29f,
            30f,
            31.5f, 33f, 34.5f, 36f,
            38f, 40f, 42f, 44f,
            46f, 48f, 50f, 52f,
            54f, 56f, 58f, 60f,
        )
    }

    sealed class State {

        data class StartState(val id: Int) : State()
        data class ReadyState(
            val song: Song,
            val chord: Chord,
            val songMeta: SongMeta,
            val chordMeta: ChordMeta,
            val lyricsOffset: Float,
            val chordsOffset: Offset,
            val chordsScrolling: Boolean,
            val chordsSpeeding: Boolean,
            val speed: Float, // TODO move in chord meta and rename zoom fields
        ) : State()
    }

    private val _state = MutableStateFlow<State>(State.StartState(id = songId))
    val state = _state.asStateFlow()

    val hiddenTonalities = SettingsRepo(application).hiddenTonalities

    init {
        viewModelScope.launch {
            val song = TheDatabase.getInstance(application).songDao().getDataById(songId)
            val chord = TheDatabase.getInstance(application).chordDao().getDataByParent(songId).firstOrNull()
            if (song != null && chord != null) {
                val songMeta =
                    (TheDatabase.getInstance(application).songDao().getMetaById(song.id) ?: SongMeta(song.id)).visit()
                TheDatabase.getInstance(application).songDao().upsert(songMeta)
                val chordMeta = TheDatabase.getInstance(application).chordDao().getMetaById(chord.id)
                    ?: ChordMeta(chord.id)
                _state.update {
                    State.ReadyState(
                        song = song,
                        chord = chord,
                        songMeta = songMeta,
                        chordMeta = chordMeta,
                        lyricsOffset = SongLyricsFocusControl.MAX_OFFSET,
                        chordsOffset = SongChordsFocusControl.maxOffset,
                        chordsScrolling = false,
                        chordsSpeeding = false,
                        speed = 30f, // TODO default speed
                    )
                }
                TheAnalytics.logScreenView(
                    screenClass = song.permalink.replace(BASE_URL, "/"),
                    screenName = song.title,
                )
            }
        }
    }

    fun setStarred(starred: Boolean) {
        val songMeta = when (state.value) {
            is State.ReadyState -> (state.value as State.ReadyState).songMeta
            else -> throw Error()
        }.copy(starred = starred)
        _state.update {
            when (it) {
                is State.ReadyState -> it.copy(songMeta = songMeta)
                else -> throw Error()
            }
        }
        viewModelScope.launch {
            TheDatabase.getInstance(application).songDao().upsert(songMeta)
        }
    }

    fun setTonality(tonality: MusicNote?) {
        val chordMeta = when (state.value) {
            is State.ReadyState -> (state.value as State.ReadyState).chordMeta
            else -> throw Error()
        }.copy(tonality = tonality)
        _state.update {
            when (it) {
                is State.ReadyState -> it.copy(chordMeta = chordMeta)
                else -> throw Error()
            }
        }
        viewModelScope.launch {
            TheDatabase.getInstance(application).chordDao().upsert(chordMeta)
        }
    }

    fun setSongScale(scale: Float) {
        val songMeta = when (state.value) {
            is State.ReadyState -> (state.value as State.ReadyState).songMeta
            else -> throw Error()
        }.copy(zoom = scale)
        _state.update {
            when (it) {
                is State.ReadyState -> it.copy(songMeta = songMeta)
                else -> throw Error()
            }
        }
        viewModelScope.launch {
            TheDatabase.getInstance(application).songDao().upsert(songMeta)
        }
    }

    fun setChordScale(scale: Float) {
        val chordMeta = when (state.value) {
            is State.ReadyState -> (state.value as State.ReadyState).chordMeta
            else -> throw Error()
        }.copy(zoom = scale)
        _state.update {
            when (it) {
                is State.ReadyState -> it.copy(chordMeta = chordMeta)
                else -> throw Error()
            }
        }
        viewModelScope.launch {
            TheDatabase.getInstance(application).chordDao().upsert(chordMeta)
        }
    }

    fun setLyricsOffset(offset: Float) {
        _state.update {
            when (it) {
                is State.ReadyState -> it.copy(lyricsOffset = offset)
                else -> throw Error()
            }
        }
    }

    fun setChordsOffset(offset: Offset) {
        _state.update {
            when (it) {
                is State.ReadyState -> it.copy(chordsOffset = offset)
                else -> throw Error()
            }
        }
    }

    fun setChordsScrolling(scrolling: Boolean) {
        _state.update {
            when (it) {
                is State.ReadyState -> it.copy(chordsScrolling = scrolling)
                else -> throw Error()
            }
        }
    }

    fun setChordsSpeeding(speeding: Boolean) {
        _state.update {
            when (it) {
                is State.ReadyState -> it.copy(chordsSpeeding = speeding)
                else -> throw Error()
            }
        }
    }

    fun setSpeed(speed: Float) {
        _state.update {
            when (it) {
                is State.ReadyState -> it.copy(speed = speed)
                else -> throw Error()
            }
        }
    }
}
