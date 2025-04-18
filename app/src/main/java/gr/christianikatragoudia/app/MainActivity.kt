package gr.christianikatragoudia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gr.christianikatragoudia.app.data.ThemeOption
import gr.christianikatragoudia.app.nav.TheNavHost
import gr.christianikatragoudia.app.network.TheAnalytics
import gr.christianikatragoudia.app.ui.theme.ChristianikaTragoudiaTheme

class MainActivity : ComponentActivity() {

    private val factory = viewModelFactory {
        initializer {
            MainViewModel(
                application = this[APPLICATION_KEY] as TheApplication,
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val viewModel: MainViewModel = viewModel(factory = factory)
            val themeOption by viewModel.themeOption.collectAsState(initial = ThemeOption.SYSTEM)
            val useDarkTheme = when (themeOption) {
                ThemeOption.SYSTEM -> isSystemInDarkTheme()
                ThemeOption.LIGHT -> false
                ThemeOption.DARK -> true
            }
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = Color.Transparent.toArgb(),
                    darkScrim = Color.Transparent.toArgb(),
                    detectDarkMode = {
                        useDarkTheme
                    },
                ),
                navigationBarStyle = SystemBarStyle.auto(
                    lightScrim = Color.Transparent.toArgb(),
                    darkScrim = Color.Transparent.toArgb(),
                    detectDarkMode = {
                        useDarkTheme
                    }
                )
            )
            ChristianikaTragoudiaTheme(useDarkTheme = useDarkTheme) {
                TheNavHost()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        TheAnalytics.logAppOpen()
    }
}
