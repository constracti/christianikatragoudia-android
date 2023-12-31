package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SongTitle
import gr.christianikatragoudia.app.network.TheAnalytics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StarredViewModel(application: TheApplication) : ViewModel() {

    private val analyticsClass = "/starred/"
    private val analyticsName =
        application.getString(R.string.starred) + " – " + application.getString(R.string.app_name)

    init {
        viewModelScope.launch {
            TheAnalytics.logScreenView(analyticsClass, analyticsName)
        }
    }

    data class UiState(
        val resultList: List<SongTitle> = listOf(),
        val loading: Boolean = true,
    )

    val uiState = application.getDatabase().songDao().getTitlesByStarred().map {
        UiState(resultList = it, loading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(resultList = listOf(), loading = true),
    )
}
