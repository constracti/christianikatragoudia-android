package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SongTitle
import gr.christianikatragoudia.app.network.TheAnalytics
import gr.christianikatragoudia.app.network.WebApp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpdateViewModel(private val application: TheApplication) : ViewModel() {

    private val analyticsClass = "/options/update/"
    private val analyticsName =
        application.getString(R.string.update) + " – " + application.getString(R.string.app_name)

    private val _snackbarMessage = MutableSharedFlow<String?>()
    val snackbarMessageFlow = _snackbarMessage.asSharedFlow()

    private fun setSnackbarMessage(message: String?) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    data class UiState(
        val loading: Boolean = true,
        val actions: List<Pair<SongTitle, Boolean>>? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkPatch()
        viewModelScope.launch {
            TheAnalytics.logScreenView(analyticsClass, analyticsName)
        }
    }

    fun checkPatch() {
        _uiState.update {
            it.copy(loading = true, actions = null)
        }
        viewModelScope.launch {
            try {
                val after = application.getSettings().getUpdateTimestamp()
                val patch = WebApp.retrofitService.getPatch(after, false)

                val oldSongMap = application.getDatabase().songDao().getAll().associateBy { it.id }
                val oldChordMap = application.getDatabase().chordDao().getAll().associateBy { it.id }
                val oldParentMap = oldChordMap.values.groupBy { it.parent }
                val oldPairMap = oldSongMap.filterKeys {
                    oldParentMap.containsKey(it)
                }.mapValues {
                    Pair(it.value, oldParentMap[it.key]!![0])
                }

                val newSongMap = oldSongMap.filterKeys { patch.songIdSet.contains(it) }.toMutableMap()
                patch.songList.forEach {
                    newSongMap[it.id] = it
                }
                val newChordMap = oldChordMap.filterKeys { patch.chordIdSet.contains(it) }.toMutableMap()
                patch.chordList.forEach {
                    newChordMap[it.id] = it
                }
                val newParentMap = newChordMap.values.groupBy { it.parent }
                val newPairMap = newSongMap.filterKeys {
                    newParentMap.containsKey(it)
                }.mapValues {
                    Pair(it.value, newParentMap[it.key]!![0])
                }

                val actions = mutableListOf<Pair<SongTitle, Boolean>>()
                newPairMap.values.sortedBy {
                    val song = it.first
                    val chord = it.second
                    maxOf(song.modified, chord.modified)
                }.forEach {
                    val song = it.first
                    val chord = it.second
                    val pair = oldPairMap[song.id]
                    if (pair == null)
                        actions.add(Pair(SongTitle(song.id, song.title, song.excerpt), false))
                    else if (pair.first.modified != song.modified || pair.second.modified != chord.modified)
                        actions.add(Pair(SongTitle(song.id, song.title, song.excerpt), true))
                }

                _uiState.update {
                    it.copy(loading = false, actions = actions)
                }
            } catch (e: Exception) {
                setSnackbarMessage(application.getString(R.string.download_error_message))
                _uiState.update {
                    it.copy(loading = false)
                }
            }
            TheAnalytics.logUpdateCheck()
        }
    }

    fun applyPatch() {
        _uiState.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            try {
                val after = application.getSettings().getUpdateTimestamp()
                val patch = WebApp.retrofitService.getPatch(after, true)

                val oldSongMap = application.getDatabase().songDao().getAll().associateBy { it.id }
                val oldChordMap = application.getDatabase().chordDao().getAll().associateBy { it.id }

                val insSongList = patch.songList.filter {
                    !oldSongMap.containsKey(it.id)
                }
                val updSongList = patch.songList.filter {
                    oldSongMap.containsKey(it.id)
                }
                val delSongList = oldSongMap.filterKeys {
                    !patch.songIdSet.contains(it)
                }.values

                val insChordList = patch.chordList.filter {
                    !oldChordMap.containsKey(it.id)
                }
                val updChordList = patch.chordList.filter {
                    oldChordMap.containsKey(it.id)
                }
                val delChordList = oldChordMap.filterKeys {
                    !patch.chordIdSet.contains(it)
                }.values

                application.getDatabase().songDao().insert(*insSongList.toTypedArray())
                application.getDatabase().songDao().update(*updSongList.toTypedArray())
                application.getDatabase().songDao().delete(*delSongList.toTypedArray())

                application.getDatabase().chordDao().insert(*insChordList.toTypedArray())
                application.getDatabase().chordDao().update(*updChordList.toTypedArray())
                application.getDatabase().chordDao().delete(*delChordList.toTypedArray())

                application.getSettings().setUpdateTimestamp(patch.timestamp)
                _uiState.update {
                    it.copy(loading = false, actions = listOf())
                }
            } catch (e: Exception) {
                setSnackbarMessage(application.getString(R.string.download_error_message))
                _uiState.update {
                    it.copy(loading = false)
                }
            }
            TheAnalytics.logUpdateApply()
        }
    }
}
