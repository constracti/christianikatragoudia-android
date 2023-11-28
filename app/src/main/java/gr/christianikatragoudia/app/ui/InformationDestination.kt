package gr.christianikatragoudia.app.ui

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import gr.christianikatragoudia.app.R
import gr.christianikatragoudia.app.nav.NavDestination
import gr.christianikatragoudia.app.network.BASE_URL
import gr.christianikatragoudia.app.network.DOMAIN_NAME
import gr.christianikatragoudia.app.network.EMAIL_ADDRESS
import gr.christianikatragoudia.app.network.TheAnalytics
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme

object InformationDestination : NavDestination {

    override val route = "information"

    private const val analyticsClass = "/info/"
    @StringRes
    private val analyticsNameRes = R.string.information

    @Composable
    fun TheScreen(
        navigateBack: () -> Unit,
    ) {
        val analyticsName = stringResource(analyticsNameRes) + " – " + stringResource(R.string.app_name)
        LaunchedEffect(Unit) {
            TheAnalytics.logScreenView(analyticsClass, analyticsName)
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
                title = stringResource(R.string.information),
                navigateBack = navigateBack,
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
            ) {
                val context = LocalContext.current
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(BASE_URL))
                    context.startActivity(intent)
                }) {
                    Text(text = stringResource(R.string.information_open_site))
                }
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_ADDRESS))
                    }
                    context.startActivity(intent)
                }) {
                    Text(text = stringResource(R.string.information_send_mail))
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
            val text = buildAnnotatedString {
                append(stringResource(R.string.information_description))
                append("\n\n")
                append(stringResource(R.string.information_features))
                append("\n\n")
                append(stringResource(R.string.information_extras))
                append("\n")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(DOMAIN_NAME)
                }
                append("\n\n")
                append(stringResource(R.string.information_contribution))
                append("\n\n")
                append(stringResource(R.string.information_developer))
                append("\n")
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(EMAIL_ADDRESS)
                }
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
