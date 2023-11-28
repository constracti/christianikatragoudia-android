package gr.christianikatragoudia.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.data.Version
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme
import kotlinx.coroutines.flow.collectLatest

object OptionsDestination : NavDestination {

    override val route = "options"

    private val factory = viewModelFactory {
        initializer {
            OptionsViewModel(
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    @Composable
    fun TheScreen(
        navigateToSearch: () -> Unit,
        navigateToStarred: () -> Unit,
        navigateToRecent: () -> Unit,
        navigateToTonalities: () -> Unit,
        navigateToTheme: () -> Unit,
        navigateToInformation: () -> Unit,
        navigateToLicense: () -> Unit,
        navigateToVersions: () -> Unit,
        viewModel: OptionsViewModel = viewModel(factory = factory),
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
        TheScaffold(
            snackbarHostState = snackbarHostState,
            navigateToSearch = navigateToSearch,
            navigateToStarred = navigateToStarred,
            navigateToRecent = navigateToRecent,
            navigateToTonalities = navigateToTonalities,
            navigateToTheme = navigateToTheme,
            onSynchronizeClick = {
                viewModel.synchronize()
            },
            onClearRecentClick = {
                viewModel.clearRecent()
            },
            onTonalityResetClick = {
                viewModel.resetTonality()
            },
            onZoomResetClick = {
                viewModel.resetZoom()
            },
            navigateToInformation = navigateToInformation,
            navigateToLicense = navigateToLicense,
            navigateToVersions = navigateToVersions,
            processing = uiState.processing,
        )
    }
}

@Composable
private fun TheScaffold(
    snackbarHostState: SnackbarHostState,
    navigateToSearch: () -> Unit,
    navigateToStarred: () -> Unit,
    navigateToRecent: () -> Unit,
    navigateToTonalities: () -> Unit,
    navigateToTheme: () -> Unit,
    onSynchronizeClick: () -> Unit,
    onClearRecentClick: () -> Unit,
    onZoomResetClick: () -> Unit,
    onTonalityResetClick: () -> Unit,
    navigateToInformation: () -> Unit,
    navigateToLicense: () -> Unit,
    navigateToVersions: () -> Unit,
    processing: Boolean,
) {
    Scaffold(
        topBar = {
            TheTopAppBar(
                title = stringResource(R.string.options),
                navigateBack = navigateToSearch,
            )
        },
        bottomBar = {
            TheNavigationBar(
                selected = TheNavigationBarScreen.OPTIONS,
                navigateToSearch = navigateToSearch,
                navigateToStarred = navigateToStarred,
                navigateToRecent = navigateToRecent,
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ListHeader(text = stringResource(R.string.settings))
                NavigationListItem(
                    headline = stringResource(R.string.settings_tonalities_title),
                    support = stringResource(R.string.settings_tonalities_support),
                    enabled = !processing,
                    onClick = navigateToTonalities,
                )
                NavigationListItem(
                    headline = stringResource(R.string.theme),
                    enabled = !processing,
                    onClick = navigateToTheme,
                )
                ListHeader(text = stringResource(R.string.tools))
                AlertListItem(
                    headline = stringResource(R.string.tools_synchronize_title),
                    text = stringResource(R.string.tools_synchronize_description),
                    enabled = !processing,
                    icon = Icons.Default.Refresh,
                    confirmButtonText = stringResource(R.string.synchronize),
                    onConfirmButtonClick = onSynchronizeClick,
                )
                AlertListItem(
                    headline = stringResource(R.string.tools_recent_clear_title),
                    text = stringResource(R.string.tools_recent_clear_description),
                    enabled = !processing,
                    icon = Icons.Default.Warning,
                    confirmButtonText = stringResource(R.string.clear),
                    onConfirmButtonClick = onClearRecentClick,
                )
                AlertListItem(
                    headline = stringResource(R.string.tools_tonality_reset_title),
                    text = stringResource(R.string.tools_tonality_reset_description),
                    enabled = !processing,
                    icon = Icons.Default.Warning,
                    confirmButtonText = stringResource(R.string.reset),
                    onConfirmButtonClick = onTonalityResetClick,
                )
                AlertListItem(
                    headline = stringResource(R.string.tools_font_size_reset_title),
                    text = stringResource(R.string.tools_font_size_reset_description),
                    enabled = !processing,
                    icon = Icons.Default.Warning,
                    confirmButtonText = stringResource(R.string.reset),
                    onConfirmButtonClick = onZoomResetClick,
                )
                ListHeader(text = stringResource(R.string.application))
                NavigationListItem(
                    headline = stringResource(R.string.information),
                    support = null,
                    enabled = !processing,
                    onClick = navigateToInformation,
                )
                NavigationListItem(
                    headline = stringResource(R.string.license),
                    support = stringResource(R.string.license_short),
                    enabled = !processing,
                    onClick = navigateToLicense,
                )
                NavigationListItem(
                    headline = stringResource(R.string.version),
                    support = Version.CURRENT,
                    enabled = !processing,
                    onClick = navigateToVersions,
                )
            }
            if (processing) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun ListHeader(text: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(48.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun NavigationListItem(
    headline: String,
    support: String? = null,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(text = headline) },
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        supportingContent = { if (support != null) Text(text = support) },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun AlertListItem(
    headline: String,
    support: String? = null,
    text: String,
    enabled: Boolean,
    icon: ImageVector,
    confirmButtonText: String,
    onConfirmButtonClick: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(text = headline) },
        modifier = Modifier.clickable(enabled) { visible = true },
        supportingContent = { if (support != null) Text(text = support) },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
    if (visible) {
        AlertDialog(
            onDismissRequest = { visible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        visible = false
                        onConfirmButtonClick()
                    },
                ) {
                    Text(text = confirmButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = { visible = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            icon = { Icon(imageVector = icon, contentDescription = null) },
            title = { Text(text = headline) },
            text = { Text(text = text) },
        )
    }
}

@Preview
@Composable
private fun ThePreview() {
    ChristianikaTragoudiaTheme {
        TheScaffold(
            snackbarHostState = SnackbarHostState(),
            navigateToSearch = {},
            navigateToStarred = {},
            navigateToRecent = {},
            navigateToTonalities = {},
            navigateToTheme = {},
            onSynchronizeClick = {},
            onClearRecentClick = {},
            onTonalityResetClick = {},
            onZoomResetClick = {},
            navigateToInformation = {},
            navigateToLicense = {},
            navigateToVersions = {},
            processing = false,
        )
    }
}
