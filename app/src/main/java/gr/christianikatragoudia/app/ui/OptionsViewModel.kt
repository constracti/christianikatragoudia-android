package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.network.TheAnalytics
import gr.christianikatragoudia.app.network.WebApp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OptionsViewModel(private val application: TheApplication) : ViewModel() {

    private val analyticsClass = "/options/"
    private val analyticsName =
        application.getString(R.string.options) + " – " + application.getString(R.string.app_name)

    private val _snackbarMessage = MutableSharedFlow<String?>()
    val snackbarMessageFlow = _snackbarMessage.asSharedFlow()

    private fun setSnackbarMessage(message: String?) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    data class UiState(
        val processing: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            TheAnalytics.logScreenView(analyticsClass, analyticsName)
        }
    }

    fun synchronize() {
        _uiState.update {
            it.copy(processing = true)
        }
        viewModelScope.launch {
            try {

                val newSongMap = WebApp.retrofitService.getSongs().associateBy { it.id }
                val newChordMap = WebApp.retrofitService.getChords().associateBy { it.id }

                val oldSongMap = application.getDatabase().songDao().getAll().associateBy { it.id }
                val insertSongList = newSongMap.filterKeys { id ->
                    !oldSongMap.containsKey(id)
                }.values
                val updateSongList = newSongMap.filter { (id, newSong) ->
                    val oldSong = oldSongMap[id]
                    oldSong != null && !oldSong.isSynchronized(newSong)
                }.values
                val deleteSongList = oldSongMap.filterKeys { id ->
                    !newSongMap.containsKey(id)
                }.values
                application.getDatabase().songDao().insert(*insertSongList.toTypedArray())
                application.getDatabase().songDao().update(*updateSongList.toTypedArray())
                application.getDatabase().songDao().delete(*deleteSongList.toTypedArray())

                val oldChordMap =
                    application.getDatabase().chordDao().getAll().associateBy { it.id }
                val insertChordList = newChordMap.filterKeys { id ->
                    !oldChordMap.containsKey(id)
                }.values
                val updateChordList = newChordMap.filter { (id, newChord) ->
                    val oldChord = oldChordMap[id]
                    oldChord != null && !oldChord.isSynchronized(newChord)
                }.values
                val deleteChordList = oldChordMap.filterKeys { id ->
                    !newChordMap.containsKey(id)
                }.values
                application.getDatabase().chordDao().insert(*insertChordList.toTypedArray())
                application.getDatabase().chordDao().update(*updateChordList.toTypedArray())
                application.getDatabase().chordDao().delete(*deleteChordList.toTypedArray())

                setSnackbarMessage(application.getString(R.string.tools_synchronize_success))
                _uiState.update {
                    it.copy(processing = false)
                }

            } catch (e: Exception) {
                setSnackbarMessage(application.getString(R.string.download_error_message))
                _uiState.update {
                    it.copy(processing = false)
                }
            }
            TheAnalytics.logSynchronize()
        }
    }

    fun clearRecent() {
        _uiState.update {
            it.copy(processing = true)
        }
        viewModelScope.launch {
            application.getDatabase().songMetaDao().clearRecent()
            _uiState.update {
                it.copy(processing = false)
            }
        }
    }

    fun resetTonality() {
        _uiState.update {
            it.copy(processing = true)
        }
        viewModelScope.launch {
            application.getDatabase().chordMetaDao().resetTonality()
            _uiState.update {
                it.copy(processing = false)
            }
        }
    }

    fun resetZoom() {
        _uiState.update {
            it.copy(processing = true)
        }
        viewModelScope.launch {
            application.getDatabase().songMetaDao().resetZoom()
            application.getDatabase().chordMetaDao().resetZoom()
            _uiState.update {
                it.copy(processing = false)
            }
        }
    }
}
