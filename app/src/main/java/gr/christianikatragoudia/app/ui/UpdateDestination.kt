package gr.christianikatragoudia.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.Patch
import gr.christianikatragoudia.app.data.SongTitle
import gr.christianikatragoudia.app.nav.NavDestination
import kotlinx.coroutines.flow.collectLatest

object UpdateDestination : NavDestination {

    override val route = "update"

    private val factory = viewModelFactory {
        initializer {
            UpdateViewModel(
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    @Composable
    fun TheScreen(
        navigateBack: () -> Unit,
        viewModel: UpdateViewModel = viewModel(factory = factory),
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(Unit) {
            viewModel.snackbarMessageFlow.collectLatest {
                if (it != null) {
                    snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
                }
            }
        }
        val uiState by viewModel.uiState.collectAsState()
        val loading = uiState.loading
        val actions = uiState.actions
        Scaffold(
            topBar = {
                TheTopAppBar(
                    title = stringResource(R.string.update),
                    navigateBack = navigateBack,
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = Color.Transparent,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = { viewModel.checkPatch() },
                            enabled = !loading,
                        ) {
                            Text(text = "Έλεγχος")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(
                            onClick = { viewModel.applyPatch() },
                            enabled = !loading && !actions.isNullOrEmpty(),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_download_24),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text(text = "Λήψη")
                        }
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                )
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            if (loading) {
                LoadingBox(modifier = Modifier.padding(it))
            } else if (!actions.isNullOrEmpty()) {
                ListContent(actions = actions, modifier = Modifier.padding(it))
            } else if (actions != null) {
                SuccessContent(modifier = Modifier.padding(it))
            }
        }
    }
}

@Composable
private fun ListContent(
    actions: Map<Patch.Action, List<SongTitle>>,
    modifier: Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        actions.forEach { (action, resultList) ->
            item {
                ResultHeader(text = stringResource(action.text))
            }
            items(resultList) {
                ResultItem(songTitle = it)
            }
        }
    }
}

@Composable
private fun SuccessContent(
    modifier: Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5F),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.update_success),
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
