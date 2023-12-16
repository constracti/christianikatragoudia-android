package gr.christianikatragoudia.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.network.DOMAIN_NAME
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme
import kotlinx.coroutines.flow.collectLatest

object WelcomeDestination : NavDestination {

    override val route = "welcome"

    private val factory = viewModelFactory {
        initializer {
            WelcomeViewModel(
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    @Composable
    fun TheScreen(
        navigateToSearch: () -> Unit,
        navigateToInformation: () -> Unit,
        navigateToLicense: () -> Unit,
        viewModel: WelcomeViewModel = viewModel(factory = factory),
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
        LaunchedEffect(uiState.passed) {
            if (uiState.passed) {
                navigateToSearch()
            }
        }
        if (uiState.loading || uiState.passed) {
            LoadingScreen()
        } else {
            TheScaffold(
                snackbarHostState = snackbarHostState,
                onDownloadButtonClick = {
                    viewModel.applyPatch()
                },
                navigateToInformation = navigateToInformation,
                navigateToLicense = navigateToLicense,
            )
        }
    }
}

@Composable
private fun TheScaffold(
    snackbarHostState: SnackbarHostState,
    onDownloadButtonClick: () -> Unit,
    navigateToInformation: () -> Unit,
    navigateToLicense: () -> Unit,
) {
    Scaffold(
        topBar = {
            TheTopAppBar()
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
            ) {
                TextButton(onClick = navigateToInformation) {
                    Text(text = stringResource(R.string.information))
                }
                TextButton(onClick = navigateToLicense) {
                    Text(text = stringResource(R.string.license))
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        Column(
            modifier = Modifier.padding(it).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val text = buildAnnotatedString {
                append(stringResource(R.string.welcome_prompt))
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(DOMAIN_NAME)
                }
            }
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.size(24.dp))
            Button(onClick = onDownloadButtonClick) {
                Icon(
                    painter = painterResource(R.drawable.baseline_download_24),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.download))
            }
        }
    }
}

@Preview
@Composable
private fun EmptyBodyPreview() {
    ChristianikaTragoudiaTheme {
        TheScaffold(
            snackbarHostState = SnackbarHostState(),
            onDownloadButtonClick = {},
            navigateToInformation = {},
            navigateToLicense = {},
        )
    }
}
