package gr.christianikatragoudia.app.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.data.Version
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.network.TheAnalytics
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme
import java.time.format.DateTimeFormatter

object VersionsDestination : NavDestination {

    override val route = "versions"

    private const val analyticsClass = "/versions/"
    @StringRes
    private val analyticsNameRes = R.string.version_history

    @Composable
    fun TheScreen(
        navigateBack: () -> Unit,
    ) {
        val analyticsName = stringResource(analyticsNameRes) + " – " + stringResource(R.string.app_name)
        LaunchedEffect(Unit) {
            TheAnalytics.logScreenView(analyticsClass, analyticsName)
        }
        TheScaffold(navigateBack = navigateBack)
    }
}

@Composable
private fun TheScaffold(
    navigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TheTopAppBar(
                title = stringResource(R.string.version_history),
                navigateBack = navigateBack,
            )
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth(),
            contentPadding = PaddingValues(4.dp),
        ) {
            items(Version.entries.reversed()) { version ->
                Row(
                    modifier = Modifier.padding(4.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${stringResource(R.string.version)} ${version.tag}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = version.date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                stringArrayResource(version.changes).forEach { change ->
                    Text(
                        text = "- $change",
                        modifier = Modifier.padding(4.dp),
                    )
                }
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Preview
@Composable
private fun ThePreview() {
    ChristianikaTragoudiaTheme {
        TheScaffold(navigateBack = {})
    }
}
