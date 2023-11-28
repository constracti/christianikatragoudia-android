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
            val count = application.getDatabase().songDao().count()
            _uiState.update {
                it.copy(
                    loading = false,
                    passed = count > 0,
                )
            }
            if (count == 0)
                TheAnalytics.logScreenView(analyticsClass, analyticsName)
        }
    }

    fun getWebData() {
        _uiState.update {
            it.copy(loading = true)
        }
        viewModelScope.launch {
            try {
                val songList = WebApp.retrofitService.getSongs()
                val chordList = WebApp.retrofitService.getChords()
                application.getDatabase().songDao().insert(*songList.toTypedArray())
                application.getDatabase().chordDao().insert(*chordList.toTypedArray())
                val count = application.getDatabase().songDao().count()
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
            TheAnalytics.logSynchronize()
        }
    }
}
