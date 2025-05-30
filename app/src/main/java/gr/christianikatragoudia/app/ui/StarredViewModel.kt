package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SettingsRepo
import gr.christianikatragoudia.app.data.SongTitle
import gr.christianikatragoudia.app.data.TheDatabase
import gr.christianikatragoudia.app.network.TheAnalytics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StarredViewModel(application: TheApplication) : ViewModel() {

    init {
        viewModelScope.launch {
            TheAnalytics.logScreenView(
                screenClass = "/starred/",
                screenName = application.getString(R.string.starred),
            )
        }
    }

    val updateCheck = SettingsRepo(application).updateCheck

    data class UiState(
        val resultList: List<SongTitle> = listOf(),
        val loading: Boolean = true,
    )

    val uiState = TheDatabase.getInstance(application).songDao().getTitlesByStarred().map {
        UiState(resultList = it.map { songTitle -> songTitle.simplifyExcerpt() }, loading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(resultList = listOf(), loading = true),
    )
}
