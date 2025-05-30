package gr.christianikatragoudia.app.ui

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.network.TheAnalytics
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme

object LicenseDestination : NavDestination {

    override val route = "license"

    @Composable
    fun TheScreen(
        navigateBack: () -> Unit,
    ) {
        val analyticsName = stringResource(R.string.license)
        LaunchedEffect(Unit) {
            TheAnalytics.logScreenView("/license/", analyticsName)
        }
        TheScaffold(
            navigateBack = navigateBack,
        )
    }
}

@Composable
private fun TheScaffold(
    navigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TheTopAppBar(
                title = stringResource(R.string.license),
                navigateBack = navigateBack,
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
            ) {
                val context = LocalContext.current
                TextButton(onClick = {
                    val uri = "https://creativecommons.org/licenses/by-nc-sa/4.0/deed.el".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }) {
                    Text(text = stringResource(R.string.details))
                }
            }
        },
        contentColor = MaterialTheme.colorScheme.onBackground,
        containerColor = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
        ) {
            val margin = 24.dp
            Text(text = stringResource(R.string.license_long))
            Spacer(modifier = Modifier.height(margin))
            Image(painter = painterResource(id = R.drawable.cc_by_nc_sa), contentDescription = null)
            HorizontalDivider(modifier = Modifier.padding(vertical = margin))
            val text = buildAnnotatedString {
                append(stringResource(R.string.license_introduction))
                append("\n\n")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.license_attribution_title))
                }
                append("\n")
                append(stringResource(R.string.license_attribution_content))
                append("\n\n")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.license_non_commercial_title))
                }
                append("\n")
                append(stringResource(R.string.license_non_commercial_content))
                append("\n\n")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.license_share_alike_title))
                }
                append("\n")
                append(stringResource(R.string.license_share_alike_content))
            }
            Text(text = text)
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
