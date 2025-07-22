package gr.christianikatragoudia.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SettingsRepo
import gr.christianikatragoudia.app.data.SongFts
import gr.christianikatragoudia.app.data.SongTitle
import gr.christianikatragoudia.app.data.TheDatabase
import gr.christianikatragoudia.app.network.TheAnalytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

class SearchViewModel(private val application: TheApplication) : ViewModel() {

    data class UiState(
        val query: String = "",
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            TheAnalytics.logScreenView(
                screenClass = "/search/",
                screenName = application.getString(R.string.search),
            )
        }
    }

    val updateCheck = SettingsRepo(application).updateCheck

    fun setQuery(query: String) {
        _uiState.update {
            it.copy(query = query)
        }
    }

    fun getResultListFlow(query: String): Flow<List<SongTitle>> {
        if (query.isEmpty()) {
            return TheDatabase.getInstance(application).songDao().getTitles().map {
                it.map { songTitle -> songTitle.simplifyExcerpt() }
            }
        }
        val tokenList = SongFts.tokenize(query).split(" ").filter { token ->
            token.isNotEmpty()
        }
        val maxLength = tokenList.count()
        val minLength = max(maxLength - 2, 1)
        val termList = mutableListOf<String>()
        for (length in minLength..maxLength) {
            for (start in 0..(maxLength - length)) {
                val stop = start + length
                val subList = tokenList.subList(start, stop)
                termList.add(subList.joinToString(" "))
                termList.add(subList.joinToString(" ") { token ->
                    "${token}*"
                })
            }
        }
        val expr = termList.joinToString(" OR ") { term ->
            "\"${term}\""
        }
        return TheDatabase.getInstance(application).songDao().getMatchesByQuery(expr).map {
            it.map { songMatch ->
                Pair(SongTitle(songMatch), -songMatch.getScore())
            }.sortedBy { pair -> pair.second }.map { pair -> pair.first }
        }
    }
}
