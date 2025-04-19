package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SettingsRepo
import gr.christianikatragoudia.app.data.SongFts
import gr.christianikatragoudia.app.data.TheDatabase
import gr.christianikatragoudia.app.network.TheAnalytics
import gr.christianikatragoudia.app.network.WebApp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WelcomeViewModel(private val application: TheApplication) : ViewModel() {

    private val analyticsClass = "/welcome/"
    private val analyticsName =
        application.getString(R.string.welcome) + " – " + application.getString(R.string.app_name)

    private val _snackbarMessage = MutableSharedFlow<String?>()
    val snackbarMessageFlow = _snackbarMessage.asSharedFlow()

    private fun setSnackbarMessage(message: String?) {
        viewModelScope.launch {
            _snackbarMessage.emit(message)
        }
    }

    data class UiState(
        val loading: Boolean = true,
        val passed: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val countData = TheDatabase.getInstance(application).songDao().countData()
            val countFts = TheDatabase.getInstance(application).songDao().countFts()
            if (countData > 0 && countFts == 0) {
                val songList = TheDatabase.getInstance(application).songDao().getDataList()
                for (song in songList) {
                    TheDatabase.getInstance(application).songDao().insert(SongFts(song))
                }
                TheDatabase.getInstance(application).songDao().optimize()
            }
            _uiState.update {
                it.copy(
                    loading = false,
                    passed = countData > 0,
                )
            }
            if (countData == 0)
                TheAnalytics.logScreenView(analyticsClass, analyticsName)
        }
    }

    fun applyPatch() {
        _uiState.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            try {
                val patch = WebApp.retrofitService.getPatch(null, true)
                for (song in patch.songList) {
                    TheDatabase.getInstance(application).songDao().insert(song)
                    TheDatabase.getInstance(application).songDao().insert(SongFts(song))
                }
                TheDatabase.getInstance(application).songDao().optimize()
                for (chord in patch.chordList) {
                    TheDatabase.getInstance(application).chordDao().insert(chord)
                }
                SettingsRepo(application).setUpdateTimestamp(patch.timestamp)
                SettingsRepo(application).setUpdateCheck(false)
                val count = TheDatabase.getInstance(application).songDao().countData()
                _uiState.update {
                    it.copy(
                        loading = false,
                        passed = count > 0,
                    )
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
