package gr.christianikatragoudia.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import gr.christianikatragoudia.app.R

// TODO implement TheScaffold with container and content color set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheTopAppBar(
    title: String = stringResource(R.string.app_name),
    navigateBack: (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            if (navigateBack != null) {
                IconButton(onClick = navigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LoadingScreen(
    title: String = stringResource(R.string.app_name),
    navigateBack: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            TheTopAppBar(title, navigateBack)
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        LoadingBox(modifier = Modifier.padding(it))
    }
}

@Composable
fun ErrorScreen(
    title: String = stringResource(R.string.app_name),
    message: String = stringResource(R.string.error),
    navigateBack: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            TheTopAppBar(title, navigateBack)
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Box(
            modifier = Modifier.padding(it),
            contentAlignment = Alignment.Center,
        ) {
            Text(message)
        }
    }
}

enum class TheNavigationBarScreen {
    SEARCH,
    STARRED,
    RECENT,
    OPTIONS,
}

@Composable
fun TheNavigationBar(
    selected: TheNavigationBarScreen,
    navigateToSearch: () -> Unit = {},
    navigateToStarred: () -> Unit = {},
    navigateToRecent: () -> Unit = {},
    navigateToOptions: () -> Unit = {},
) {
    NavigationBar(
        containerColor = Color.Transparent,
    ) {
        NavigationBarItem(
            selected = selected == TheNavigationBarScreen.SEARCH,
            onClick = navigateToSearch,
            icon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            label = {
                Text(text = stringResource(R.string.search))
            },
        )
        NavigationBarItem(
            selected = selected == TheNavigationBarScreen.STARRED,
            onClick = navigateToStarred,
            icon = {
                Icon(imageVector = Icons.Default.Star, contentDescription = null)
            },
            label = {
                Text(text = stringResource(R.string.starred))
            },
        )
        NavigationBarItem(
            selected = selected == TheNavigationBarScreen.RECENT,
            onClick = navigateToRecent,
            icon = {
                Icon(painter = painterResource(R.drawable.baseline_history_24), contentDescription = null)
            },
            label = {
                Text(text = stringResource(R.string.recent))
            },
        )
        NavigationBarItem(
            selected = selected == TheNavigationBarScreen.OPTIONS,
            onClick = navigateToOptions,
            icon = {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null)
            },
            label = {
                Text(text = stringResource(R.string.options))
            },
        )
    }
}
