package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.music.MusicNote
import gr.christianikatragoudia.app.network.TheAnalytics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TonalitiesViewModel(private val application: TheApplication) : ViewModel() {

    private val analyticsClass = "/options/tonalities/"
    private val analyticsName =
        application.getString(R.string.settings_tonalities_title) + " – " + application.getString(R.string.app_name)

    init {
        viewModelScope.launch {
            TheAnalytics.logScreenView(analyticsClass, analyticsName)
        }
    }

    data class UiState(
        val hiddenTonalities: Set<MusicNote>,
        val loading: Boolean = true,
    )

    val uiState = application.getSettings().hiddenTonalities.map {
        UiState(hiddenTonalities = it, loading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(hiddenTonalities = setOf(), loading = true),
    )

    fun toggle(musicNote: MusicNote) {
        val hidden = uiState.value.hiddenTonalities.toMutableSet()
        if (hidden.contains(musicNote))
            hidden.remove(musicNote)
        else
            hidden.add(musicNote)
        viewModelScope.launch {
            application.getSettings().setHiddenTonalities(hidden)
        }
    }

    fun reset() {
        viewModelScope.launch {
            application.getSettings().setHiddenTonalities(null)
        }
    }
}
