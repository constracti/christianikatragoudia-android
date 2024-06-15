package gr.christianikatragoudia.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.SongTitle
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme

object SearchDestination : NavDestination {

    override val route = "search"

    private val factory = viewModelFactory {
        initializer {
            SearchViewModel(
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    @Composable
    fun TheScreen(
        navigateToStarred: () -> Unit,
        navigateToRecent: () -> Unit,
        navigateToOptions: () -> Unit,
        navigateToUpdate: () -> Unit,
        navigateToSong: (Int) -> Unit,
        viewModel: SearchViewModel = viewModel(factory = factory),
    ) {
        val uiState = viewModel.uiState.collectAsState()
        val query = uiState.value.query
        val resultList by viewModel.getResultListFlow(query).collectAsState(initial = emptyList())
        val updateCheck by viewModel.updateCheck.collectAsState(initial = false)
        TheScaffold(
            navigateToStarred = navigateToStarred,
            navigateToRecent = navigateToRecent,
            navigateToOptions = navigateToOptions,
            navigateToUpdate = navigateToUpdate,
            query = query,
            onQueryFieldValueChange = {
                viewModel.setQuery(it)
            },
            updateCheck = updateCheck,
            resultList = resultList,
            navigateToSong = navigateToSong,
        )
    }
}

@Composable
private fun TheScaffold(
    navigateToStarred: () -> Unit,
    navigateToRecent: () -> Unit,
    navigateToOptions: () -> Unit,
    navigateToUpdate: () -> Unit,
    query: String,
    onQueryFieldValueChange: (String) -> Unit,
    updateCheck: Boolean,
    resultList: List<SongTitle>,
    navigateToSong: (Int) -> Unit,
) {
    Scaffold(
        bottomBar = {
            TheNavigationBar(
                selected = TheNavigationBarScreen.SEARCH,
                navigateToStarred = navigateToStarred,
                navigateToRecent = navigateToRecent,
                navigateToOptions = navigateToOptions,
            )
        },
        floatingActionButton = {
            if (updateCheck) {
                FloatingActionButton(onClick = navigateToUpdate) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_sync_24),
                        contentDescription = stringResource(R.string.update),
                    )
                }
            }
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        Column(modifier = Modifier
            .padding(it)
            .fillMaxWidth()) {
            SearchForm(
                query = query,
                onQueryFieldValueChange = onQueryFieldValueChange,
            )
            ResultList(
                resultList = resultList,
                navigateToSong = navigateToSong,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchForm(
    query: String,
    onQueryFieldValueChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible)
            focusManager.clearFocus()
    }
    OutlinedTextField(
        value = query,
        onValueChange = onQueryFieldValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        label = {
            Text(stringResource(R.string.search))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search),
            )
        },
        trailingIcon = {
            if (query != "") {
                IconButton(onClick = { onQueryFieldValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear),
                    )
                }
            }
        },
        singleLine = true,
    )
}

@Preview
@Composable
private fun ThePreview() {
    ChristianikaTragoudiaTheme {
        TheScaffold(
            navigateToStarred = {},
            navigateToRecent = {},
            navigateToOptions = {},
            navigateToUpdate = {},
            query = "",
            onQueryFieldValueChange = {},
            updateCheck = true,
            resultList = listOf(
                SongTitle(1, "Θαβώρ", "Θ' ανεβούμε μαζί στο βουνό"),
                SongTitle(2, "Ευωδία Χριστού", "Στης αγάπης τον ήλιο"),
            ),
            navigateToSong = {},
        )
    }
}
