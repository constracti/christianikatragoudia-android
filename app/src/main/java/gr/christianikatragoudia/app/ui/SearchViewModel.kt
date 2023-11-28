package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SongTitle
import gr.christianikatragoudia.app.network.TheAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(private val application: TheApplication) : ViewModel() {

    private val analyticsClass = "/search/"
    private val analyticsName =
        application.getString(R.string.search) + " – " + application.getString(R.string.app_name)

    data class UiState(
        val query: String = "",
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            TheAnalytics.logScreenView(analyticsClass, analyticsName)        }
    }

    // TODO combine query and results in StateFlow

    fun setQuery(query: String) {
        _uiState.update {
            it.copy(query = query)
        }
        TheAnalytics.logSearch(query)
    }

    fun getResultListFlow(query: String): Flow<List<SongTitle>> {
        return application.getDatabase().songDao().getTitlesByQuery("%$query%")
    }
}
