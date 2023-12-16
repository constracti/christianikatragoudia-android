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
