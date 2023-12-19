package gr.christianikatragoudia.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SongTitle
import gr.christianikatragoudia.app.nav.NavDestination

object RecentDestination : NavDestination {

    override val route = "recent"

    private val factory = viewModelFactory {
        initializer {
            RecentViewModel(
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    @Composable
    fun TheScreen(
        navigateToSearch: () -> Unit,
        navigateToStarred: () -> Unit,
        navigateToOptions: () -> Unit,
        navigateToSong: (Int) -> Unit,
        viewModel: RecentViewModel = viewModel(factory = factory),
    ) {
        val uiState = viewModel.uiState.collectAsState().value
        TheScaffold(
            navigateToSearch = navigateToSearch,
            navigateToStarred = navigateToStarred,
            navigateToOptions = navigateToOptions,
            resultList = uiState.resultList,
            navigateToSong = navigateToSong,
        )
    }
}

@Composable
private fun TheScaffold(
    navigateToSearch: () -> Unit,
    navigateToStarred: () -> Unit,
    navigateToOptions: () -> Unit,
    resultList: List<SongTitle>,
    navigateToSong: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TheTopAppBar(
                title = stringResource(R.string.recent),
                navigateBack = navigateToSearch,
            )
        },
        bottomBar = {
            TheNavigationBar(
                selected = TheNavigationBarScreen.RECENT,
                navigateToSearch = navigateToSearch,
                navigateToStarred = navigateToStarred,
                navigateToOptions = navigateToOptions,
            )
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        ResultList(
            resultList = resultList,
            modifier = Modifier.padding(it),
            navigateToSong = navigateToSong,
        )
    }
}
