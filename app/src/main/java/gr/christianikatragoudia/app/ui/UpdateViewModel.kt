package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.NotificationRepo
import gr.christianikatragoudia.app.data.Patch
import gr.christianikatragoudia.app.data.SettingsRepo
import gr.christianikatragoudia.app.data.SongTitle
import gr.christianikatragoudia.app.data.TheDatabase
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
        val actions: MutableMap<Patch.Action, MutableList<SongTitle>>? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        NotificationRepo(application).cancelNewContentNotification()
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
                val after = SettingsRepo(application).getUpdateTimestamp()
                val patch = WebApp.retrofitService.getPatch(after, false)

                val oldSongMap = TheDatabase.getInstance(application).songDao().getAll().associateBy { it.id }
                val oldChordMap = TheDatabase.getInstance(application).chordDao().getAll().associateBy { it.id }
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

                SettingsRepo(application).setNotificationTimestamp(patch.timestamp)

                val actions = mutableMapOf<Patch.Action, MutableList<SongTitle>>()
                newPairMap.values.sortedWith(compareBy(
                    {it.first.title},
                    {it.first.excerpt},
                    {it.first.id},
                )).forEach {
                    val song = it.first
                    val chord = it.second
                    val pair = oldPairMap[song.id]
                    val action = if (pair == null)
                        Patch.Action.ADD
                    else if (pair.first.modified != song.modified || pair.second.modified != chord.modified)
                        Patch.Action.EDIT
                    else
                        null
                    if (action != null) {
                        if (!actions.containsKey(action))
                            actions[action] = mutableListOf()
                        actions[action]!!.add(SongTitle(song.id, song.title, song.excerpt))
                    }
                }

                _uiState.update {
                    it.copy(loading = false, actions = actions.toSortedMap())
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
                val after = SettingsRepo(application).getUpdateTimestamp()
                val patch = WebApp.retrofitService.getPatch(after, true)

                val oldSongMap = TheDatabase.getInstance(application).songDao().getAll().associateBy { it.id }
                val oldChordMap = TheDatabase.getInstance(application).chordDao().getAll().associateBy { it.id }

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

                TheDatabase.getInstance(application).songDao().insert(*insSongList.toTypedArray())
                TheDatabase.getInstance(application).songDao().update(*updSongList.toTypedArray())
                TheDatabase.getInstance(application).songDao().delete(*delSongList.toTypedArray())

                TheDatabase.getInstance(application).chordDao().insert(*insChordList.toTypedArray())
                TheDatabase.getInstance(application).chordDao().update(*updChordList.toTypedArray())
                TheDatabase.getInstance(application).chordDao().delete(*delChordList.toTypedArray())

                SettingsRepo(application).setUpdateTimestamp(patch.timestamp)
                SettingsRepo(application).setNotificationTimestamp(patch.timestamp)
                _uiState.update {
                    it.copy(loading = false, actions = mutableMapOf())
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
