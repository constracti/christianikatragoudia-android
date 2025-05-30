package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SettingsRepo
import gr.christianikatragoudia.app.data.TheDatabase
import gr.christianikatragoudia.app.network.TheAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OptionsViewModel(private val application: TheApplication) : ViewModel() {

    data class UiState(
        val processing: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            TheAnalytics.logScreenView(
                screenClass = "/options/",
                screenName = application.getString(R.string.options),
            )
        }
    }

    val updateCheck = SettingsRepo(application).updateCheck

    fun clearRecent() {
        _uiState.update {
            it.copy(processing = true)
        }
        viewModelScope.launch {
            TheDatabase.getInstance(application).songDao().clearRecent()
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
            TheDatabase.getInstance(application).chordDao().resetTonality()
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
            TheDatabase.getInstance(application).songDao().resetZoom()
            TheDatabase.getInstance(application).chordDao().resetZoom()
            _uiState.update {
                it.copy(processing = false)
            }
        }
    }
}
