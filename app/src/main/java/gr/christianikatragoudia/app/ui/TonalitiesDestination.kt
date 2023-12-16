package gr.christianikatragoudia.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.TheApplication
import gr.christianikatragoudia.app.music.MusicNote
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme

object TonalitiesDestination : NavDestination {

    override val route = "tonalities"

    private val factory = viewModelFactory {
        initializer {
            TonalitiesViewModel(
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    @Composable
    fun DestinationScreen(
        navigateBack: () -> Unit,
        viewModel: TonalitiesViewModel = viewModel(factory = factory),
    ) {
        val uiState = viewModel.uiState.collectAsState().value
        TheScaffold(
            hiddenTonalities = uiState.hiddenTonalities,
            loading = uiState.loading,
            onTonalityToggle = {
                viewModel.toggle(it)
            },
            onTonalityReset = {
                viewModel.reset()
            },
            navigateBack = navigateBack,
        )
    }
}

@Composable
private fun TheScaffold(
    hiddenTonalities: Set<MusicNote>,
    loading: Boolean,
    onTonalityToggle: (MusicNote) -> Unit,
    onTonalityReset: () -> Unit,
    navigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TheTopAppBar(
                title = stringResource(R.string.tonalities),
                navigateBack = navigateBack,
            )
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        if (loading) {
            LoadingBox(modifier = Modifier.padding(it))
        } else {
            TheBody(
                hiddenTonalities = hiddenTonalities,
                onTonalityToggle = onTonalityToggle,
                onTonalityReset = onTonalityReset,
                modifier = Modifier.padding(it),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TheBody(
    hiddenTonalities: Set<MusicNote>,
    onTonalityToggle: (MusicNote) -> Unit,
    onTonalityReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bodyMargin = 4.dp
    val cardMargin = 8.dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bodyMargin),
    ) {
        Card(
            modifier = Modifier.padding(bodyMargin),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5F),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(cardMargin))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.padding(vertical = cardMargin),
                )
                Spacer(modifier = Modifier.width(cardMargin))
                Text(
                    text = stringResource(R.string.tonalities_support),
                    modifier = Modifier.padding(vertical = cardMargin),
                )
                Spacer(modifier = Modifier.width(cardMargin))
            }
        }
        Divider(modifier = Modifier.padding(bodyMargin))
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            maxItemsInEachRow = 3,
        ) {
            MusicNote.TONALITIES.forEach { musicNote ->
                Card(
                    modifier = Modifier
                        .weight(1F)
                        .padding(bodyMargin),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5F),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(cardMargin),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = MusicNote.toNotation(musicNote))
                        Switch(
                            checked = !hiddenTonalities.contains(musicNote),
                            onCheckedChange = {
                                onTonalityToggle(musicNote)
                            },
                        )
                    }
                }
            }
        }
        TextButton(
            onClick = onTonalityReset,
            modifier = Modifier
                .align(Alignment.End)
                .padding(bodyMargin),
        ) {
            Text(text = stringResource(R.string.reset))
        }
    }
}

@Preview
@Composable
private fun ThePreview() {
    ChristianikaTragoudiaTheme {
        TheScaffold(
            hiddenTonalities = MusicNote.ENHARMONIC_TONALITIES,
            loading = false,
            onTonalityToggle = {},
            onTonalityReset = {},
            navigateBack = {}
        )
    }
}
