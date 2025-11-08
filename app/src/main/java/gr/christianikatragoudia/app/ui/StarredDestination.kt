package gr.christianikatragoudia.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

object StarredDestination : NavDestination {

    override val route = "starred"

    private val factory = viewModelFactory {
        initializer {
            StarredViewModel(
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    @Composable
    fun TheScreen(
        navigateToSearch: () -> Unit,
        navigateToRecent: () -> Unit,
        navigateToOptions: () -> Unit,
        navigateToSong: (Int) -> Unit,
        viewModel: StarredViewModel = viewModel(factory = factory),
    ) {
        val uiState by viewModel.uiState.collectAsState()
        val updateCheck by viewModel.updateCheck.collectAsState(initial = false)
        TheScaffold(
            navigateToSearch = navigateToSearch,
            navigateToRecent = navigateToRecent,
            navigateToOptions = navigateToOptions,
            updateCheck = updateCheck,
            resultList = uiState.resultList,
            navigateToSong = navigateToSong,
        )
    }
}

@Composable
private fun TheScaffold(
    navigateToSearch: () -> Unit,
    navigateToRecent: () -> Unit,
    navigateToOptions: () -> Unit,
    updateCheck: Boolean,
    resultList: List<SongTitle>,
    navigateToSong: (Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TheTopAppBar(
                title = stringResource(R.string.starred),
                navigateBack = navigateToSearch,
            )
        },
        bottomBar = {
            TheNavigationBar(
                selected = TheNavigationBarScreen.STARRED,
                navigateToSearch = navigateToSearch,
                navigateToRecent = navigateToRecent,
                navigateToOptions = navigateToOptions,
                reddenOptions = updateCheck,
            )
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) { paddingValues ->
        ResultList(
            resultList = resultList,
            modifier = Modifier.padding(paddingValues = paddingValues),
            navigateToSong = navigateToSong,
        )
    }
}
